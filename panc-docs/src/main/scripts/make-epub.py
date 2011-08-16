#!/usr/bin/env python

import zipfile, os, sys

MIMETYPE = 'mimetype'

def remove_epub_files(path=None):
    if (not path == None):
        if (os.path.isdir(path)):
            for f in os.listdir(path):
                if (f.endswith('.epub')):
                    full_path = os.path.join(path, f)
                    print 'Deleting: %s' % full_path
                    os.remove(full_path)


def create_archive(path='/path/to/our/epub/directory'):
    '''Create the ZIP archive.  The mimetype must be the first file in the archive 
    and it must not be compressed.'''

    epub_name = '%s.epub' % os.path.basename(path)
    epub_path = os.path.join('../', epub_name)

    print "epub output file = '%s'" % epub_path

    # The EPUB must contain the META-INF and mimetype files at the root, so 
    # we'll create the archive in the working directory first and move it later
    old_dir = os.getcwd()
    os.chdir(path)    

    # Open a new zipfile for writing
    epub = zipfile.ZipFile(epub_path, 'w')

    # Add the mimetype file first and set it to be uncompressed
    epub.write(MIMETYPE, compress_type=zipfile.ZIP_STORED)
    
    # For the remaining paths in the EPUB, add all of their files
    # using normal ZIP compression
    process_files(epub, epub_name, '.')
            
    epub.close()

    os.chdir(old_dir)


def process_files(epub, epub_name, path):
    if (os.path.isdir(path)):
        for f in os.listdir(path):
            process_files(epub, epub_name, os.path.join(path, f))
    else:
        write_compressed_file(epub, epub_name, path)


def write_compressed_file(epub, epub_name, full_path):
    name = os.path.basename(full_path)
    if (name != epub_name and name != 'mimetype'):
        epub.write(full_path, compress_type=zipfile.ZIP_DEFLATED)


def process_root_directories(path=None):
    if (not path == None):
        if (os.path.isdir(path)):
            for f in os.listdir(path):
                full_path = os.path.join(path, f)
                if (os.path.isdir(full_path)):
                    print 'Processing: %s' % full_path
                    create_archive(full_path)


if __name__ == "__main__":
    remove_epub_files(sys.argv[1])
    process_root_directories(sys.argv[1])
