" Omni Completion for PAN
" Language:     Pan
" Maintainer:   Jerome Pansanel <jerome.pansanel@iphc.cnrs.fr>
" Last Change:  2011 March 25
" Location:     https://quattor.svn.sourceforge.net/svnroot/quattor/trunk/util/vim/ftplugin/pan.vim
" License:      Apache License, Version 2.0
"
" Please download most recent version first before mailing any comments.
" Based on the Pan language v8.4.7

if v:version < 700
  echohl ERROR "Smart completion will work only in vim version 7+"
  finish
endif

"kind type: v variable; f : function or method; m : member of struct or class, t: typedef; d: define or macro

let s:TYPE_LIST = [ 'boolean', 'double', 'link', 'list', 'long', 'nlist', 'string' ]

let s:STATEMENT_LIST = [ 'bind', 'final', 'include', 'function', 'type', 'valid', 'variable' ]

let s:TEMPLATE_LIST = [ 'declaration', 'object', 'structure', 'template', 'unique' ]

let s:BUILTIN_FUNCTION_LIST = [
\ {'kind': 'f', 'word': 'append', 'abbr': 'append()', 'menu': 'list append(value)'},
\ {'kind': 'f', 'word': 'append', 'abbr': 'append()', 'menu': 'list append(target,value)', 'dup': 1},
\ {'kind': 'f', 'word': 'base64_decode', 'abbr': 'base64_decode()', 'menu': 'string base64_decode(encoded)'},
\ {'kind': 'f', 'word': 'base64_encode', 'abbr': 'base64_encode()', 'menu': 'string base64_encode(unencoded)'},
\ {'kind': 'f', 'word': 'clone', 'abbr': 'clone()', 'menu': 'element clone(arg)'},
\ {'kind': 'f', 'word': 'create', 'abbr': 'create()', 'menu': 'nlist create(tpl_name, )'},
\ {'kind': 'f', 'word': 'debug', 'abbr': 'debug()', 'menu': 'string debug(msg)'},
\ {'kind': 'f', 'word': 'delete', 'abbr': 'delete()', 'menu': 'undef delete(arg)'},
\ {'kind': 'f', 'word': 'deprecated', 'abbr': 'deprecated()', 'menu': 'string deprecated(level, msg)'},
\ {'kind': 'f', 'word': 'error', 'abbr': 'error()', 'menu': 'void error(msg)'},
\ {'kind': 'f', 'word': 'escape', 'abbr': 'escape()', 'menu': 'string escape(str)'},
\ {'kind': 'f', 'word': 'exists', 'abbr': 'exists()', 'menu': 'boolean exists(var)'},
\ {'kind': 'f', 'word': 'first', 'abbr': 'first()', 'menu': 'boolean first(r, key, value)'},
\ {'kind': 'f', 'word': 'format', 'abbr': 'format()', 'menu': 'string format(fmt, param, )'},
\ {'kind': 'f', 'word': 'if_exists', 'abbr': 'if_exists()', 'menu': 'string|undef if_exists(tpl)'},
\ {'kind': 'f', 'word': 'index', 'abbr': 'index()', 'menu': 'long index(sub, arg, start)'},
\ {'kind': 'f', 'word': 'index', 'abbr': 'index()', 'menu': 'long index(sub, list, start)', 'dup': 1},
\ {'kind': 'f', 'word': 'index', 'abbr': 'index()', 'menu': 'string index(sub, arg, start)', 'dup': 1},
\ {'kind': 'f', 'word': 'is_boolean', 'abbr': 'is_boolean()', 'menu': 'boolean is_boolean(arg)'},
\ {'kind': 'f', 'word': 'is_defined', 'abbr': 'is_defined()', 'menu': 'boolean is_defined(arg)'},
\ {'kind': 'f', 'word': 'is_double', 'abbr': 'is_double()', 'menu': 'boolean is_double(arg)'},
\ {'kind': 'f', 'word': 'is_list', 'abbr': 'is_list()', 'menu': 'boolean is_list(arg)'},
\ {'kind': 'f', 'word': 'is_long', 'abbr': 'is_long()', 'menu': 'boolean is_long(arg)'},
\ {'kind': 'f', 'word': 'is_nlist', 'abbr': 'is_nlist()', 'menu': 'boolean is_nlist(arg)'},
\ {'kind': 'f', 'word': 'is_null', 'abbr': 'is_null()', 'menu': 'boolean is_null(arg)'},
\ {'kind': 'f', 'word': 'is_number', 'abbr': 'is_number()', 'menu': 'boolean is_number(arg)'},
\ {'kind': 'f', 'word': 'is_property', 'abbr': 'is_property()', 'menu': 'boolean is_property(arg)'},
\ {'kind': 'f', 'word': 'is_resource', 'abbr': 'is_resource()', 'menu': 'boolean is_resource(arg)'},
\ {'kind': 'f', 'word': 'is_string', 'abbr': 'is_string()', 'menu': 'boolean is_string(arg)'},
\ {'kind': 'f', 'word': 'key', 'abbr': 'key()', 'menu': 'string key(resource, index)'},
\ {'kind': 'f', 'word': 'length', 'abbr': 'length()', 'menu': 'long length(str)', 'dup': 1},
\ {'kind': 'f', 'word': 'length', 'abbr': 'length()', 'menu': 'long length(res)'},
\ {'kind': 'f', 'word': 'list', 'abbr': 'list()', 'menu': 'list list(elem, )'},
\ {'kind': 'f', 'word': 'match', 'abbr': 'match()', 'menu': 'boolean match(target, regex)'},
\ {'kind': 'f', 'word': 'matches', 'abbr': 'matches()', 'menu': 'string[] matches(target, regex)'},
\ {'kind': 'f', 'word': 'merge', 'abbr': 'merge()', 'menu': 'resource merge(res1, res2, )'},
\ {'kind': 'f', 'word': 'nlist', 'abbr': 'nlist()', 'menu': 'nlist nlist(key, property, )'},
\ {'kind': 'f', 'word': 'next', 'abbr': 'next()', 'menu': 'boolean next(res, key, value)'},
\ {'kind': 'f', 'word': 'path_exists', 'abbr': 'path_exists()', 'menu': 'boolean path_exists(path)'},
\ {'kind': 'f', 'word': 'prepend', 'abbr': 'prepend()', 'menu': 'list prepend(value)'},
\ {'kind': 'f', 'word': 'prepend', 'abbr': 'prepend()', 'menu': 'list prepend(target, value)', 'dup': 1},
\ {'kind': 'f', 'word': 'replace', 'abbr': 'replace()', 'menu': 'string replace(regex, repl, target)'},
\ {'kind': 'f', 'word': 'return', 'abbr': 'return()', 'menu': 'element return(value)'},
\ {'kind': 'f', 'word': 'splice', 'abbr': 'splice()', 'menu': 'string splice(str, start, length, repl)'},
\ {'kind': 'f', 'word': 'split', 'abbr': 'split()', 'menu': 'string[] split(regex, target)'},
\ {'kind': 'f', 'word': 'split', 'abbr': 'split()', 'menu': 'string[] split(regex, limit, target)', 'dup': 1},
\ {'kind': 'f', 'word': 'substr', 'abbr': 'substr()', 'menu': 'string substr(target, start)'},
\ {'kind': 'f', 'word': 'substr', 'abbr': 'substr()', 'menu': 'string substr(target, start, length)', 'dup': 1},
\ {'kind': 'f', 'word': 'to_boolean', 'abbr': 'to_boolean()', 'menu': 'boolean to_boolean(prop)'},
\ {'kind': 'f', 'word': 'to_double', 'abbr': 'to_double()', 'menu': 'double to_double(prop)'},
\ {'kind': 'f', 'word': 'to_long', 'abbr': 'to_long()', 'menu': 'long to_long(prop)'},
\ {'kind': 'f', 'word': 'to_lowercase', 'abbr': 'to_lowercase()', 'menu': 'string to_lowercase(target)'},
\ {'kind': 'f', 'word': 'to_string', 'abbr': 'to_string()', 'menu': 'string to_string(elem)'},
\ {'kind': 'f', 'word': 'to_uppercase', 'abbr': 'to_uppercase()', 'menu': 'string to_uppercase(target)'},
\ {'kind': 'f', 'word': 'traceback', 'abbr': 'traceback()', 'menu': 'string traceback(msg)'},
\ {'kind': 'f', 'word': 'unescape', 'abbr': 'unescape()', 'menu': 'string unescape(str)'},
\ {'kind': 'f', 'word': 'value', 'abbr': 'value()', 'menu': 'element value(path)'} ]

" This function is used for the 'omnifunc' option.
function! pancomplete#Complete(findstart, base)
  if a:findstart
    " Locate the start of the item, including ".", "->" and "[...]".
    let line = getline('.')
    let start = col('.') - 1
    while start > 0 && line[start -1] =~ '\a'
      let start -= 1
    endwhile
    return start
  else
    "remember cursor position
    let line = line('.')
    let col = col('.')
    "vim no longer moves the cursor upon completion... fix that
    let res = []
    let line = getline('.')
    for typ in s:TYPE_LIST
      if typ =~ '^' . a:base
        call complete_add(typ)
      endif
    endfor
    for stt in s:STATEMENT_LIST
      if stt =~ '^' . a:base
        call complete_add(stt)
      endif
    endfor
    for tpl in s:TEMPLATE_LIST
      if tpl =~ '^' . a:base
        call complete_add(tpl)
      endif
    endfor
    for blt in s:BUILTIN_FUNCTION_LIST
      if blt['word'] =~ '^' . a:base
        call complete_add(blt)
      endif
    endfor
    return []
  endif
endfunction

if exists('g:syntastic_extra_filetypes')
    call add(g:syntastic_extra_filetypes, 'pan')
else
    let g:syntastic_extra_filetypes = ['pan']
endif
