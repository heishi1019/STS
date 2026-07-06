package com.heishi.bioliterature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heishi.bioliterature.dto.PaperDetailResponse.AuthorSummary;
import com.heishi.bioliterature.dto.PaperDetailResponse.KeywordSummary;
import com.heishi.bioliterature.dto.PaperDetailResponse.TagSummary;
import com.heishi.bioliterature.entity.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

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
}
