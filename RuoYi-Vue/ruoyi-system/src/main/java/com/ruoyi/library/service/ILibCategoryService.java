package com.ruoyi.library.service;

import com.ruoyi.library.domain.LibCategory;

import java.util.List;

public interface ILibCategoryService {
    List<LibCategory> selectLibCategoryList(LibCategory category);

    LibCategory selectLibCategoryById(Long categoryId);

    int insertLibCategory(LibCategory category);

    int updateLibCategory(LibCategory category);

    int moveLibCategory(LibCategory category);

    int deleteLibCategoryByIds(Long[] categoryIds);
}
