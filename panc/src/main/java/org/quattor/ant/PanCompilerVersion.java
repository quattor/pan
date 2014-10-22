/*
 Copyright (c) 2006-2012 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.quattor.ant;

import java.io.File;
import java.util.LinkedList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.quattor.pan.Compiler;

/**
 * An ant task which displays the pan compiler version.
 * 
 * @author Michel Jouvin
 * 
 */
public class PanCompilerVersion extends Task {

    @Override
    public void execute() throws BuildException {

        this.log("pan compiler version: "+Compiler.version);

    }

}
