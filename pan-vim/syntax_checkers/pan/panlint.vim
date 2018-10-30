if exists('g:loaded_syntastic_pan_panlint_checker')
    finish
endif
let g:loaded_syntastic_pan_panlint_checker = 1

if !exists('g:syntastic_pan_panlint_sort')
    let g:syntastic_pan_panlint_sort = 1
endif

let s:save_cpo = &cpo
set cpo&vim

function! SyntaxCheckers_pan_panlint_IsAvailable() dict
    return executable(self.getExec())
endfunction

function! SyntaxCheckers_pan_panlint_GetLocList() dict
    let makeprg = self.makeprgBuild({})

    let errorformat = '%f:%l: %m'

    return SyntasticMake({
        \ 'makeprg': makeprg,
        \ 'errorformat': errorformat,
        \ 'defaults': { 'type': 'W', 'subtype': 'Style' },
        \ 'subtype': 'Style'})

endfunction

call g:SyntasticRegistry.CreateAndRegisterChecker({
            \ 'filetype': 'pan',
            \ 'name': 'panlint'})

let &cpo = s:save_cpo
unlet s:save_cpo

"vim: set sw=4 sts=4 et fdm=marker:
