Installation
============

If you want to enable PAN syntax highlighting or code completion, the first
step is to tell to Vim how to recognize PAN files. To do this, create the
following directory:
mkdir -p ${HOME}/.vim/ftdetect

and download the following file from the quattor website:
https://quattor.svn.sourceforge.net/svnroot/quattor/trunk/util/vim/ftdetect/pan.vim
and copy it to the ${HOME}/.vim/ftdetect directory.

=== Syntax Highlighting ===

For highlighting PAN keywords, create the syntax directory with the following
command:
mkdir ${HOME}/.vim/syntax

Then download the following file from the quattor website:
https://quattor.svn.sourceforge.net/svnroot/quattor/trunk/util/vim/syntax/pan.vim
and copy it to the ${HOME}/.vim/syntax directory.

=== PAN Code Completion ===

For enabling code auto-completion, add the following line in your .vimrc file:
filetype plugin on

Then create the autoload and ftplugin directories with the following commands:
mkdir ${HOME}/.vim/autoload
mkdir ${HOME}/.vim/ftplugin

Put in the autoload directory the following file:
https://quattor.svn.sourceforge.net/svnroot/quattor/trunk/util/vim/autoload/pancomplete.vim

Put in the ftplugin directory the following file:
https://quattor.svn.sourceforge.net/svnroot/quattor/trunk/util/vim/ftplugin/pan.vim

Code completion is performed by using Ctrl-x + Ctrl-o in insert or append mode.
