import request from '@/utils/request'

// 查询图书列表
export function listBook(query) {
    return request({
        url: '/library/book/list',
        method: 'get',
        params: query
    })
}

export function addBook(data) {
    return request({
        url: '/library/book/add',
        method: 'post',
        data: data
    })
}

// 查询图书详细
export function getBook(bookId) {
  return request({
    url: '/library/book/' + bookId,
    method: 'get'
  })
}

// 修改图书
export function updateBook(data) {
  return request({
    url: '/library/book/edit',
    method: 'put',
    data: data
  })
}

// 删除图书
export function delBook(bookIds) {
  return request({
    url: '/library/book/remove/' + bookIds,
    method: 'delete'
  })
}