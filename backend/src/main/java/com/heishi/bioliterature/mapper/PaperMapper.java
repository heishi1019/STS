package com.heishi.bioliterature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heishi.bioliterature.dto.BiomedicalEntitySummary;
import com.heishi.bioliterature.dto.PaperDetailResponse.AuthorSummary;
import com.heishi.bioliterature.dto.PaperDetailResponse.KeywordSummary;
import com.heishi.bioliterature.dto.PaperDetailResponse.TagSummary;
import com.heishi.bioliterature.dto.PaperReferenceSummary;
import com.heishi.bioliterature.dto.TopicSummary;
import com.heishi.bioliterature.entity.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    @Select("""
            <script>
            SELECT DISTINCT p.*
            FROM paper p
            <where>
                <if test="title != null and title != ''">
                    AND p.title LIKE CONCAT('%', #{title}, '%')
                </if>
                <if test="author != null and author != ''">
                    AND EXISTS (
                        SELECT 1
                        FROM paper_author pa
                        INNER JOIN author a ON a.id = pa.author_id
                        WHERE pa.paper_id = p.id
                          AND (a.full_name LIKE CONCAT('%', #{author}, '%')
                               OR a.normalized_name LIKE CONCAT('%', #{author}, '%'))
                    )
                </if>
                <if test="keyword != null and keyword != ''">
                    AND EXISTS (
                        SELECT 1
                        FROM paper_keyword pk
                        INNER JOIN keyword k ON k.id = pk.keyword_id
                        WHERE pk.paper_id = p.id
                          AND (k.name LIKE CONCAT('%', #{keyword}, '%')
                               OR k.normalized_name LIKE CONCAT('%', #{keyword}, '%'))
                    )
                </if>
                <if test="journal != null and journal != ''">
                    AND p.journal LIKE CONCAT('%', #{journal}, '%')
                </if>
                <if test="doi != null and doi != ''">
                    AND p.doi LIKE CONCAT('%', #{doi}, '%')
                </if>
                <if test="pmid != null and pmid != ''">
                    AND p.pmid = #{pmid}
                </if>
                <if test="year != null">
                    AND p.publication_year = #{year}
                </if>
                <if test="dataSource != null and dataSource != ''">
                    AND p.data_source = #{dataSource}
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
            </where>
            ORDER BY p.publication_year DESC, p.id DESC
            </script>
            """)
    IPage<Paper> searchPapers(
            Page<Paper> page,
            @Param("title") String title,
            @Param("author") String author,
            @Param("keyword") String keyword,
            @Param("journal") String journal,
            @Param("doi") String doi,
            @Param("pmid") String pmid,
            @Param("year") Integer year,
            @Param("dataSource") String dataSource,
            @Param("tagId") Long tagId,
            @Param("topicId") Long topicId);

    @Select("""
            SELECT a.id, a.full_name AS name
            FROM paper_author pa
            INNER JOIN author a ON a.id = pa.author_id
            WHERE pa.paper_id = #{paperId}
            ORDER BY pa.author_order ASC, pa.id ASC
            """)
    List<AuthorSummary> selectAuthorsByPaperId(@Param("paperId") Long paperId);

    @Select("""
            SELECT k.id, k.name AS keyword
            FROM paper_keyword pk
            INNER JOIN keyword k ON k.id = pk.keyword_id
            WHERE pk.paper_id = #{paperId}
            ORDER BY pk.id ASC
            """)
    List<KeywordSummary> selectKeywordsByPaperId(@Param("paperId") Long paperId);

    @Select("""
            SELECT t.id, t.name AS tag_name, t.color
            FROM paper_tag pt
            INNER JOIN tag t ON t.id = pt.tag_id
            WHERE pt.paper_id = #{paperId}
            ORDER BY t.name ASC, t.id ASC
            """)
    List<TagSummary> selectTagsByPaperId(@Param("paperId") Long paperId);

    @Select("""
            SELECT tp.id, tp.name
            FROM topic_paper rel
            INNER JOIN topic tp ON tp.id = rel.topic_id
            WHERE rel.paper_id = #{paperId}
            ORDER BY tp.name ASC, tp.id ASC
            """)
    List<TopicSummary> selectTopicsByPaperId(@Param("paperId") Long paperId);

    @Select("""
            SELECT be.id, be.entity_name, be.entity_type
            FROM paper_entity pe
            INNER JOIN biomedical_entity be ON be.id = pe.entity_id
            WHERE pe.paper_id = #{paperId}
            ORDER BY be.entity_type ASC, be.entity_name ASC
            """)
    List<BiomedicalEntitySummary> selectEntitiesByPaperId(@Param("paperId") Long paperId);

    @Select("""
            SELECT
                pr.id,
                pr.cited_paper_id,
                pr.cited_pmid,
                pr.cited_doi,
                pr.citation_text,
                cited.title AS cited_title
            FROM paper_reference pr
            LEFT JOIN paper cited ON cited.id = pr.cited_paper_id
            WHERE pr.paper_id = #{paperId}
            ORDER BY pr.id ASC
            """)
    List<PaperReferenceSummary> selectReferencesByPaperId(@Param("paperId") Long paperId);
}
