package com.ruoyi.library.mapper;

import com.ruoyi.library.domain.LibCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LibCategoryMapper {
    List<LibCategory> selectLibCategoryList(LibCategory category);

    LibCategory selectLibCategoryById(Long categoryId);

    int insertLibCategory(LibCategory category);

    int updateLibCategory(LibCategory category);

    int moveLibCategory(LibCategory category);

    int countChildrenByCategoryId(Long categoryId);

    int countInCategorySubtree(@Param("categoryId") Long categoryId,
                               @Param("newParentId") Long newParentId);

    int countBooksByCategoryId(Long categoryId);

    LibCategory selectLibCategoryByIdForUpdate(Long categoryId);

    int deleteLibCategoryByIds(Long[] categoryIds);
}
