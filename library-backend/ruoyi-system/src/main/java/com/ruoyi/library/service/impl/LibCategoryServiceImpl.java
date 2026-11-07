package com.ruoyi.library.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.library.domain.LibCategory;
import com.ruoyi.library.mapper.LibCategoryMapper;
import com.ruoyi.library.service.ILibCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Arrays;

@Service
public class LibCategoryServiceImpl implements ILibCategoryService {

    @Autowired
    private LibCategoryMapper libCategoryMapper;

    @Override
    public List<LibCategory> selectLibCategoryList(LibCategory category) {
        return libCategoryMapper.selectLibCategoryList(category);
    }

    @Override
    public LibCategory selectLibCategoryById(Long categoryId) {
        return libCategoryMapper.selectLibCategoryById(categoryId);
    }

    @Override
    @Transactional
    public int insertLibCategory(LibCategory category) {
        Long parentId = category.getParentId();
        if (parentId == null) {
            category.setParentId(0L);
        } else if (parentId != 0L) {
            LibCategory parent = libCategoryMapper.selectLibCategoryByIdForUpdate(parentId);
            if (parent == null) {
                throw new ServiceException("上级分类不存在");
            }
        }

        return libCategoryMapper.insertLibCategory(category);
    }

    @Override
    public int updateLibCategory(LibCategory category) {
        Long categoryId = category.getCategoryId();
        if (categoryId == null) {
            throw new ServiceException("请选择要修改的分类");
        }
        if (libCategoryMapper.selectLibCategoryById(categoryId) == null) {
            throw new ServiceException("分类不存在");
        }
        return libCategoryMapper.updateLibCategory(category);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int moveLibCategory(LibCategory category) {
        Long categoryId = category.getCategoryId();
        Long newParentId = category.getParentId();
        if (categoryId == null || newParentId == null) {
            throw new ServiceException("请选择要移动的分类和新上级");
        }
        if (libCategoryMapper.selectLibCategoryByIdForUpdate(categoryId) == null) {
            throw new ServiceException("要移动的分类不存在");
        }
        if (newParentId != 0L) {
            if (libCategoryMapper.selectLibCategoryById(newParentId) == null) {
                throw new ServiceException("新上级分类不存在");
            }
            if (libCategoryMapper.countInCategorySubtree(categoryId, newParentId) > 0) {
                throw new ServiceException("不能把分类移到自己或自己的子分类下面");
            }
        }
        return libCategoryMapper.moveLibCategory(category);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int deleteLibCategoryByIds(Long[] categoryIds) {
        if (categoryIds == null || categoryIds.length == 0 || Arrays.asList(categoryIds).contains(null)) {
            throw new ServiceException("请选择要删除的分类");
        }
        Long[] ids = Arrays.stream(categoryIds).distinct().sorted().toArray(Long[]::new);
        for (Long id : ids) {
            LibCategory category = libCategoryMapper.selectLibCategoryByIdForUpdate(id);
            if (category == null) {
                throw new ServiceException("分类不存在：" + id);
            }
            if (libCategoryMapper.countChildrenByCategoryId(id) > 0) {
                throw new ServiceException("「" + category.getCategoryName() + "」下还有子分类，请先处理子分类");
            }
            if (libCategoryMapper.countBooksByCategoryId(id) > 0) {
                throw new ServiceException("仍有图书使用「" + category.getCategoryName() + "」，不能删除");
            }
        }
        return libCategoryMapper.deleteLibCategoryByIds(ids);
    }
}
