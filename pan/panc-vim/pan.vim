" Vim syntax file
" Language:	Pan
" Maintainer:	Jerome Pansanel <j.pansanel@pansanel.net>
" Last Change:	2010 February 3 
" Location:	http://www.pansanel.net/vim/syntax/pan.vim
" License:      Apache License, Version 2.0
"
" Please download most recent version first before mailing any comments.
" Based on the Pan language v8.2.8

" Quit when a (custom) syntax file was already loaded
if exists("b:current_syntax")
  finish
endif

" All keywords
"
syn keyword panConstantBoolean		false true
syn keyword panConstantMisc		SELF undef
syn keyword panFunctionCheck		is_boolean is_defined is_double is_list is_long is_nlist is_null is_number is_property is_resource is_string 
syn keyword panFunctionControl		return
syn keyword panFunctionList		append list prepend splice 
syn keyword panFunctionMisc		clone delete exists merge
syn keyword panFunctionNList		create nlist
syn keyword panFunctionProc		debug deprecated error to_lowercase to_uppercase traceback
syn keyword panFunctionRegex		match matches replace split
syn keyword panFunctionString		base64_decode base64_encode escape format substr unescape
syn keyword panFunctionTemplate		if_exists path_exists value
syn keyword panFunctionType		to_boolean to_double to_long to_string
syn keyword panInclude			include
syn keyword panStatementConditional	if else
syn keyword panStatementIterator	first index key length next
syn keyword panStatementRepeat		while for foreach
syn keyword panTypeDeclaration		bind function template type valid variable
syn keyword panTypeTemplateModifier	declaration object structure unique
syn keyword panTypeModifier		final extensible
syn keyword panTypeDeclaration		bind function template type valid variable

syn region panConstantString start=/'/ end=/'/
syn region panConstantString start=/"/ end=/"/
syn match panComment "^\s*\zs#.*$" 
syn match panComment "\s\zs#.*$" 

"" EOF Block
syn region panConstantSelfBlock matchgroup=panStatementStartEnd start=+<<\z(\I\i*\)+ end=+^\z1$+ 

hi def link panComment			Comment
hi def link panConstantString		Constant
hi def link panConstantBoolean		Constant
hi def link panConstantMisc		Constant
hi def link panFunctionCheck		Identifier
hi def link panFunctionControl		Identifier 
hi def link panFunctionList		Identifier 
hi def link panFunctionMisc		Identifier 
hi def link panFunctionNList		Identifier 
hi def link panFunctionProc		Identifier 
hi def link panFunctionRegex		Identifier 
hi def link panFunctionString		Identifier 
hi def link panFunctionTemplate		Identifier
hi def link panFunctionType		Identifier
hi def link panInclude			Include
hi def link panStatementConditional	Statement
hi def link panStatementIterator	Statement
hi def link panStatementRepeat		Statement
hi def link panStatementStartEnd	Statement
hi def link panTypeDeclaration		Type
hi def link panTypeTemplateModifier	Type
hi def link panTypeModifier		Type
hi def link panTypeDeclaration		Type

let b:current_syntax = "pan"
