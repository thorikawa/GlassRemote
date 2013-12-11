/*
 * Copyright (C) 2009-2013 adakoda
 * Copyright (C) 2009-2013 Poly's Factory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.polysfactory.myglazz.awt;

import javax.swing.SwingUtilities;

public class MyGlazz {

    private MainFrame mMainFrame;
    private static String[] mArgs;

    public MyGlazz() {
    }

    public void initialize() {
        mMainFrame = new MainFrame(mArgs);
        mMainFrame.setLocationRelativeTo(null);
        mMainFrame.setVisible(true);
        mMainFrame.setFocusable(true);
        mMainFrame.selectDevice();
    }

    public static void main(String[] args) {
        mArgs = args;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MyGlazz().initialize();
            }
        });
    }
}
