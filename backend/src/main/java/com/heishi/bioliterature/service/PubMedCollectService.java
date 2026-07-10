package com.heishi.bioliterature.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heishi.bioliterature.dto.PubMedCollectRequest;
import com.heishi.bioliterature.dto.PubMedCollectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PubMedCollectService {

    private final ObjectMapper objectMapper;

    @Value("${collector.pubmed.python-command:python}")
    private String pythonCommand;

    @Value("${collector.pubmed.script-path:../collector/pubmed_to_mysql.py}")
    private String scriptPath;

    @Value("${collector.pubmed.timeout-seconds:180}")
    private long timeoutSeconds;

    @Value("${collector.pubmed.email:${NCBI_EMAIL:}}")
    private String ncbiEmail;

    @Value("${collector.pubmed.api-key:${NCBI_API_KEY:}}")
    private String ncbiApiKey;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    public PubMedCollectResponse collect(PubMedCollectRequest request) {
        String query = request.getQuery().trim();
        int retmax = request.getRetmax();

        if (!StringUtils.hasText(ncbiEmail)) {
            throw new IllegalStateException("请先在后端环境变量 NCBI_EMAIL 中配置 NCBI 联系邮箱");
        }

        Path script = resolveScriptPath();
        DatabaseTarget database = parseDatasourceUrl(datasourceUrl);

        List<String> command = new ArrayList<>();
        command.add(pythonCommand);
        command.add(script.toString());
        command.add("--query");
        command.add(query);
        command.add("--retmax");
        command.add(String.valueOf(retmax));

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(script.getParent().toFile());
        Map<String, String> env = builder.environment();
        env.put("NCBI_EMAIL", ncbiEmail);
        if (StringUtils.hasText(ncbiApiKey)) {
            env.put("NCBI_API_KEY", ncbiApiKey);
        }
        env.put("DB_HOST", database.host());
        env.put("DB_PORT", String.valueOf(database.port()));
        env.put("DB_NAME", database.database());
        env.put("DB_USERNAME", datasourceUsername);
        env.put("DB_PASSWORD", datasourcePassword == null ? "" : datasourcePassword);

        try {
            Process process = builder.start();
            CompletableFuture<String> stdout = readStream(process.inputReader(StandardCharsets.UTF_8));
            CompletableFuture<String> stderr = readStream(process.errorReader(StandardCharsets.UTF_8));

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("PubMed 采集超时，请减少最大采集数量后重试");
            }

            int exitCode = process.exitValue();
            String output = stdout.join();
            String errorOutput = stderr.join();

            if (exitCode == 0 || exitCode == 2) {
                return parseResponse(output, query, retmax, errorOutput);
            }
            throw new IllegalStateException(cleanError(errorOutput, "PubMed 采集失败"));
        } catch (IOException exception) {
            throw new IllegalStateException("无法启动 PubMed 采集器，请确认 Python 环境和采集脚本路径正确", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("PubMed 采集被中断", exception);
        }
    }

    private PubMedCollectResponse parseResponse(
            String output,
            String query,
            int retmax,
            String errorOutput
    ) {
        try {
            JsonNode root = objectMapper.readTree(output);
            int inserted = root.path("inserted").asInt(0);
            int updated = root.path("updated").asInt(0);
            int failed = root.path("failed").asInt(0);
            String status = root.path("status").asText(failed == 0 ? "success" : "failed");

            return PubMedCollectResponse.builder()
                    .taskId(root.path("task_id").isMissingNode() ? null : root.path("task_id").asLong())
                    .query(root.path("query").asText(query))
                    .status(status)
                    .totalResults(root.path("total_results").asLong(0L))
                    .requestedCount(retmax)
                    .fetchedCount(root.path("fetched").asInt(0))
                    .successWriteCount(inserted + updated)
                    .insertedCount(inserted)
                    .updatedCount(updated)
                    .skippedDuplicateCount(0)
                    .failedCount(failed)
                    .message("当前采集器遇到重复 PMID/DOI 时会更新已有文献，不跳过重复文献")
                    .errorMessage(cleanError(errorOutput, null))
                    .build();
        } catch (IOException exception) {
            throw new IllegalStateException("PubMed 采集器返回结果解析失败", exception);
        }
    }

    private Path resolveScriptPath() {
        Path script = Path.of(scriptPath);
        if (!script.isAbsolute()) {
            script = Path.of(System.getProperty("user.dir")).resolve(script).normalize();
        }
        if (!Files.exists(script)) {
            throw new IllegalStateException("PubMed 采集脚本不存在：" + script);
        }
        return script;
    }

    private DatabaseTarget parseDatasourceUrl(String url) {
        String fallbackHost = "127.0.0.1";
        int fallbackPort = 3306;
        String fallbackDatabase = "biomed_literature";
        if (!StringUtils.hasText(url) || !url.startsWith("jdbc:mysql://")) {
            return new DatabaseTarget(fallbackHost, fallbackPort, fallbackDatabase);
        }

        String target = url.substring("jdbc:mysql://".length());
        int queryIndex = target.indexOf('?');
        if (queryIndex >= 0) {
            target = target.substring(0, queryIndex);
        }
        String[] hostAndDatabase = target.split("/", 2);
        String hostAndPort = hostAndDatabase.length > 0 ? hostAndDatabase[0] : fallbackHost;
        String database = hostAndDatabase.length > 1 && StringUtils.hasText(hostAndDatabase[1])
                ? hostAndDatabase[1]
                : fallbackDatabase;
        String[] parts = hostAndPort.split(":", 2);
        String host = StringUtils.hasText(parts[0]) ? parts[0] : fallbackHost;
        int port = fallbackPort;
        if (parts.length > 1) {
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
                port = fallbackPort;
            }
        }
        return new DatabaseTarget(host, port, database);
    }

    private CompletableFuture<String> readStream(BufferedReader reader) {
        return CompletableFuture.supplyAsync(() -> {
            try (reader) {
                return reader.lines()
                        .limit(200)
                        .collect(Collectors.joining(System.lineSeparator()));
            } catch (IOException exception) {
                throw new IllegalStateException("读取采集器输出失败", exception);
            }
        });
    }

    private String cleanError(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.length() > 1000 ? trimmed.substring(0, 1000) : trimmed;
    }

    private record DatabaseTarget(String host, int port, String database) {
    }
}
