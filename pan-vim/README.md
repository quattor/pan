Installation
============

If you use Vundle to manage your vim plugins, simple add the following line to
your `.vimrc`:
```
Plugin 'quattor/pan', {'rtp': 'pan-vim/'}
```

If you want to enable PAN syntax highlighting or code completion, the first
step is to tell to Vim how to recognize PAN files. To do this, create the
following directory:
```sh
mkdir -p ${HOME}/.vim/ftdetect
```

and download `ftdetect/pan.vim` from this repository
and copy it to the `${HOME}/.vim/ftdetect` directory.

Syntax Highlighting
-------------------

For highlighting PAN keywords, create the syntax directory with the following
command:
`mkdir ${HOME}/.vim/syntax`

Then download `syntax/pan.vim` from this repository
and copy it to the ${HOME}/.vim/syntax directory.

PAN Code Completion
-------------------

For enabling code auto-completion, add the following line in your `.vimrc` file:
```
filetype plugin on
```

Then create the autoload and ftplugin directories with the following commands:
```sh
mkdir ${HOME}/.vim/autoload
mkdir ${HOME}/.vim/ftplugin
```
Copy `autoload/pancomplete.vim` and `ftplugin/pan.vim` the directories you created.

Code completion is performed by using Ctrl-x + Ctrl-o in insert or append mode.

Using panlint with Syntastic 
============================

To enable `panlint` support in Syntastic you must make sure `panlint` is in the `$PATH`
and add the following line to your `.vimrc`:
```
let g:syntastic_pan_checkers = ['panlint']
```
