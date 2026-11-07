package com.ruoyi.web.controller.library;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.library.domain.LibCategory;
import com.ruoyi.library.service.ILibCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/library/category")
public class LibCategoryController extends BaseController {

    @Autowired
    private ILibCategoryService libCategoryService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('library:category:list')")
    public AjaxResult list(LibCategory libCategory){
        return success(libCategoryService.selectLibCategoryList(libCategory));
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("@ss.hasPermi('library:category:query')")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId) {
        LibCategory category = libCategoryService.selectLibCategoryById(categoryId);
        if (category == null) {
            return error("分类不存在");
        }
        return success(category);
    }

    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermi('library:category:add')")
    @Log(title = "图书分类", businessType = BusinessType.INSERT)
    public AjaxResult add(@Validated @RequestBody LibCategory category) {
        category.setCreateBy(getUsername());
        return toAjax(libCategoryService.insertLibCategory(category));
    }

    @PutMapping("/edit")
    @PreAuthorize("@ss.hasPermi('library:category:edit')")
    @Log(title = "图书分类", businessType = BusinessType.UPDATE)
    public AjaxResult edit(@Validated @RequestBody LibCategory category) {
        category.setUpdateBy(getUsername());
        return toAjax(libCategoryService.updateLibCategory(category));
    }

    @PutMapping("/move")
    @PreAuthorize("@ss.hasPermi('library:category:move')")
    @Log(title = "图书分类", businessType = BusinessType.UPDATE)
    public AjaxResult move(@RequestBody LibCategory category) {
        category.setUpdateBy(getUsername());
        return toAjax(libCategoryService.moveLibCategory(category));
    }

    @DeleteMapping("/remove/{categoryIds}")
    @PreAuthorize("@ss.hasPermi('library:category:remove')")
    @Log(title = "图书分类", businessType = BusinessType.DELETE)
    public AjaxResult remove(@PathVariable("categoryIds") Long[] categoryIds) {
        return toAjax(libCategoryService.deleteLibCategoryByIds(categoryIds));
    }
}
