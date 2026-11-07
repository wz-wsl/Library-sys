import request from '@/utils/request'

export function listCategory(params) {
  return request({
    url: '/library/category/list',
    method: 'get',
    params
  })
}

export function addCategory(data) {
  return request({
    url: '/library/category/add',
    method: 'post',
    data
  })
}

export function getCategory(categoryId) {
  return request({
    url: '/library/category/' + categoryId,
    method: 'get'
  })
}

export function updateCategory(data) {
  return request({
    url: '/library/category/edit',
    method: 'put',
    data
  })
}

export function delCategory(categoryIds) {
  return request({
    url: '/library/category/remove/' + categoryIds.join(','),
    method: 'delete'
  })
}

export function moveCategory(data) {
  return request({
    url: '/library/category/move',
    method: 'put',
    data
  })
}
