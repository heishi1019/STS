package com.heishi.bioliterature.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heishi.bioliterature.dto.BiomedicalEntitySummary;
import com.heishi.bioliterature.dto.PaperDetailResponse;
import com.heishi.bioliterature.dto.SearchFacetResponse;
import com.heishi.bioliterature.dto.SearchPaperResponse;
import com.heishi.bioliterature.dto.TopicSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchMapper {

    @Select("""
            <script>
            SELECT DISTINCT
                p.id,
                p.title,
                p.title_zh,
                p.abstract_text,
                p.abstract_zh,
                p.journal,
                p.publication_year AS publish_year,
                p.doi,
                p.pmid,
                p.pdf_url,
                p.full_text_url
            FROM paper p
            <where>
                <if test="q != null and q != ''">
                    AND (
                        p.title LIKE CONCAT('%', #{q}, '%')
                        OR p.abstract_text LIKE CONCAT('%', #{q}, '%')
                        OR p.journal LIKE CONCAT('%', #{q}, '%')
                        OR p.doi LIKE CONCAT('%', #{q}, '%')
                        OR p.pmid = #{q}
                        OR EXISTS (
                            SELECT 1
                            FROM paper_keyword pk
                            INNER JOIN keyword k ON k.id = pk.keyword_id
                            WHERE pk.paper_id = p.id
                              AND (k.name LIKE CONCAT('%', #{q}, '%')
                                   OR k.normalized_name LIKE CONCAT('%', #{q}, '%'))
                        )
                        OR EXISTS (
                            SELECT 1
                            FROM paper_entity pe
                            INNER JOIN biomedical_entity be ON be.id = pe.entity_id
                            WHERE pe.paper_id = p.id
                              AND be.entity_name LIKE CONCAT('%', #{q}, '%')
                        )
                    )
                </if>
                <if test="diseaseIds != null and diseaseIds.size() > 0">
                    AND EXISTS (
                        SELECT 1
                        FROM paper_entity pe
                        INNER JOIN biomedical_entity be ON be.id = pe.entity_id
                        WHERE pe.paper_id = p.id
                          AND be.entity_type = 'DISEASE'
                          AND be.id IN
                          <foreach collection="diseaseIds" item="id" open="(" separator="," close=")">
                              #{id}
                          </foreach>
                    )
                </if>
                <if test="geneIds != null and geneIds.size() > 0">
                    AND EXISTS (
                        SELECT 1
                        FROM paper_entity pe
                        INNER JOIN biomedical_entity be ON be.id = pe.entity_id
                        WHERE pe.paper_id = p.id
                          AND be.entity_type = 'GENE'
                          AND be.id IN
                          <foreach collection="geneIds" item="id" open="(" separator="," close=")">
                              #{id}
                          </foreach>
                    )
                </if>
                <if test="drugIds != null and drugIds.size() > 0">
                    AND EXISTS (
                        SELECT 1
                        FROM paper_entity pe
                        INNER JOIN biomedical_entity be ON be.id = pe.entity_id
                        WHERE pe.paper_id = p.id
                          AND be.entity_type = 'DRUG'
                          AND be.id IN
                          <foreach collection="drugIds" item="id" open="(" separator="," close=")">
                              #{id}
                          </foreach>
                    )
                </if>
                <if test="symptomIds != null and symptomIds.size() > 0">
                    AND EXISTS (
                        SELECT 1
                        FROM paper_entity pe
                        INNER JOIN biomedical_entity be ON be.id = pe.entity_id
                        WHERE pe.paper_id = p.id
                          AND be.entity_type = 'SYMPTOM'
                          AND be.id IN
                          <foreach collection="symptomIds" item="id" open="(" separator="," close=")">
                              #{id}
                        </foreach>
                    )
                </if>
                <if test="keywordIds != null and keywordIds.size() > 0">
                    AND EXISTS (
                        SELECT 1
                        FROM paper_keyword pk
                        WHERE pk.paper_id = p.id
                          AND pk.keyword_id IN
                          <foreach collection="keywordIds" item="id" open="(" separator="," close=")">
                              #{id}
                          </foreach>
                    )
                </if>
                <if test="tagId != null">
                    AND EXISTS (
                        SELECT 1
                        FROM paper_tag pt
                        WHERE pt.paper_id = p.id
                          AND pt.tag_id = #{tagId}
                    )
                </if>
                <if test="topicId != null">
                    AND EXISTS (
                        SELECT 1
                        FROM topic_paper tp
                        WHERE tp.paper_id = p.id
                          AND tp.topic_id = #{topicId}
                    )
                </if>
                <if test="journal != null and journal != ''">
                    AND p.journal = #{journal}
                </if>
                <if test="yearFrom != null">
                    AND p.publication_year &gt;= #{yearFrom}
                </if>
                <if test="yearTo != null">
                    AND p.publication_year &lt;= #{yearTo}
                </if>
                <if test="hasPdf != null and hasPdf">
                    AND p.pdf_url IS NOT NULL
                    AND p.pdf_url != ''
                </if>
            </where>
            ORDER BY p.publication_year DESC, p.id DESC
            </script>
            """)
    IPage<SearchPaperResponse> searchPapers(
            Page<SearchPaperResponse> page,
            @Param("q") String q,
            @Param("diseaseIds") List<Long> diseaseIds,
            @Param("geneIds") List<Long> geneIds,
            @Param("drugIds") List<Long> drugIds,
            @Param("symptomIds") List<Long> symptomIds,
            @Param("keywordIds") List<Long> keywordIds,
            @Param("tagId") Long tagId,
            @Param("topicId") Long topicId,
            @Param("journal") String journal,
            @Param("yearFrom") Integer yearFrom,
            @Param("yearTo") Integer yearTo,
            @Param("hasPdf") Boolean hasPdf);

    @Select("""
            SELECT id, entity_name AS name
            FROM biomedical_entity
            WHERE entity_type = #{entityType}
            ORDER BY entity_name ASC, id ASC
            """)
    List<SearchFacetResponse.Option> selectEntityOptions(@Param("entityType") String entityType);

    @Select("SELECT id, name FROM tag ORDER BY name ASC, id ASC")
    List<SearchFacetResponse.Option> selectTagOptions();

    @Select("SELECT id, name FROM keyword ORDER BY name ASC, id ASC")
    List<SearchFacetResponse.Option> selectKeywordOptions();

    @Select("SELECT id, name FROM topic ORDER BY name ASC, id ASC")
    List<SearchFacetResponse.Option> selectTopicOptions();

    @Select("""
            SELECT DISTINCT journal
            FROM paper
            WHERE journal IS NOT NULL AND journal != ''
            ORDER BY journal ASC
            """)
    List<String> selectJournals();

    @Select("""
            SELECT DISTINCT publication_year
            FROM paper
            WHERE publication_year IS NOT NULL
            ORDER BY publication_year DESC
            """)
    List<Integer> selectYears();

    @Select("""
            SELECT be.id, be.entity_name, be.entity_type
            FROM paper_entity pe
            INNER JOIN biomedical_entity be ON be.id = pe.entity_id
            WHERE pe.paper_id = #{paperId}
            ORDER BY be.entity_type ASC, be.entity_name ASC
            """)
    List<BiomedicalEntitySummary> selectEntitiesByPaperId(@Param("paperId") Long paperId);

    @Select("""
            SELECT t.id, t.name AS tag_name, t.color
            FROM paper_tag pt
            INNER JOIN tag t ON t.id = pt.tag_id
            WHERE pt.paper_id = #{paperId}
            ORDER BY t.name ASC, t.id ASC
            """)
    List<PaperDetailResponse.TagSummary> selectTagsByPaperId(@Param("paperId") Long paperId);

    @Select("""
            SELECT tp.id, tp.name
            FROM topic_paper rel
            INNER JOIN topic tp ON tp.id = rel.topic_id
            WHERE rel.paper_id = #{paperId}
            ORDER BY tp.name ASC, tp.id ASC
            """)
    List<TopicSummary> selectTopicsByPaperId(@Param("paperId") Long paperId);
}
