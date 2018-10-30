" Vim syntax file
" Language:	Pan
" Maintainer:	Jerome Pansanel <jerome.pansanel@iphc.cnrs.fr>
" Last Change:	2011 March 23
" Location:	http://www.pansanel.net/vim/syntax/pan.vim
" License:      Apache License, Version 2.0
"
" Please download most recent version first before mailing any comments.
" Based on the Pan language v8.4.7

" Quit when a (custom) syntax file was already loaded
if exists("b:current_syntax")
  finish
endif

" All keywords
"
syn keyword panConstantBoolean		false true
syn keyword panConstantFund		long double path link boolean null undef
syn keyword panConstantMisc		SELF undef
syn keyword panFunctionCheck		is_boolean is_defined is_double is_list is_long is_nlist is_null is_number is_property is_resource is_string is_dict is_valid
syn keyword panFunctionControl		return
syn keyword panFunctionList		append list prepend splice
syn keyword panFunctionMisc		clone delete exists merge
syn keyword panFunctionNList		create nlist dict
syn keyword panFunctionProc		debug deprecated error to_lowercase to_uppercase traceback
syn keyword panFunctionRegex		match matches replace split join substitute
syn keyword panFunctionString		base64_decode base64_encode escape file_contents format substr unescape file_exists json_decode json_encode digest
syn keyword panFunctionTemplate		if_exists path_exists value
syn keyword panFunctionType		to_boolean to_double to_long to_string ip4_to_long long_to_ip4
syn keyword panInclude			include
syn keyword panStatementConditional	if else
syn keyword panStatementIterator	first index key length next
syn keyword panStatementRepeat		while for foreach
syn keyword panTypeDeclaration		bind function prefix template type valid variable
syn keyword panTypeTemplateModifier	declaration object structure unique
syn keyword panTypeModifier		final extensible

syn region panConstantString start=/'/ end=/'/
syn region panConstantString start=/"/ end=/"/
syn match panComment "^\s*\zs#.*$" 
syn match panComment "\s\zs#.*$" 

"" EOF Block
syn region panConstantSelfBlock matchgroup=panStatementStartEnd start=+<<\z(\I\i*\)+ end=+^\z1$+ 

""Annotations
syn region panAnnotationBlock start="@{" end="}" 
syn region panSimpleAnnotationBlock start="@[a-zA-Z]*{" end="}" 
syn region panAnnotationBlock start="@(" end=")" 
syn region panSimpleAnnotationBlock start="@[a-zA-Z]*(" end=")" 
syn region panAnnotationBlock start="@\[" end="\]"
syn region panSimpleAnnotationBlock start="@[a-zA-Z]*\[" end="\]"

hi def link panComment			Comment
hi def link panAnnotationBlock		Comment
hi def link panSimpleAnnotationBlock	Comment
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
hi def link panConstantFund		Type

let b:current_syntax = "pan"
