/*
 * SourceSatelliteWindow.java
 *
 * Copyright (C) 2009-15 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

package org.rstudio.studio.client.workbench.views.source;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.application.Desktop;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.application.ui.CodeSearchLauncher;
import org.rstudio.studio.client.common.satellite.SatelliteWindow;
import org.rstudio.studio.client.projects.ProjectMRUList;
import org.rstudio.studio.client.workbench.FileMRUList;
import org.rstudio.studio.client.workbench.ui.FontSizeManager;
import org.rstudio.studio.client.workbench.views.buildtools.BuildCommands;
import org.rstudio.studio.client.workbench.views.source.model.SourceWindowParams;

@Singleton
public class SourceSatelliteWindow extends SatelliteWindow
                                   implements SourceSatelliteView
{

   @Inject
   public SourceSatelliteWindow(Provider<EventBus> pEventBus,
                                Provider<FontSizeManager> pFSManager, 
                                Provider<SourceSatellitePresenter> pPresenter,
                                Provider<SourceWindowManager> pWindowManager,
                                Provider<SourceWindow> pSourceWindow,
                                Provider<FileMRUList> pFileMRUList,
                                Provider<ProjectMRUList> pProjectMRUList,
                                CodeSearchLauncher launcher)
   {
      super(pEventBus, pFSManager);
      pPresenter_ = pPresenter;
      pWindowManager_ = pWindowManager;
      pSourceWindow_ = pSourceWindow;
      pFileMRUList_ = pFileMRUList;
      pProjectMRUList_ = pProjectMRUList;
   }

   @Override
   protected void onInitialize(LayoutPanel mainPanel, JavaScriptObject params)
   {
      // read the params and set up window ordinal / title
      SourceWindowParams windowParams = params.cast();
      String title = null;
      if (windowParams != null)
      {
         pWindowManager_.get().setSourceWindowOrdinal(
               windowParams.getOrdinal());
         title = windowParams.getTitle();
      }
      if (title == null)
         title = "";
      else
         title += " - ";
      title += "RStudio Source Editor";
      Window.setTitle(title);
      
      // set up the source window
      SourceWindow sourceWindow = pSourceWindow_.get();
      if (windowParams != null &&
          windowParams.getDocId() != null &&
          windowParams.getSourcePosition() != null)
      {
         // if this source window is being opened to pop out a particular doc,
         // read that doc's ID and current position so we can restore it 
         sourceWindow.setInitialDoc(windowParams.getDocId(), 
               windowParams.getSourcePosition());
      }
      
      SourceSatellitePresenter appPresenter = pPresenter_.get();
      
      // initialize build commands (we want these to work from source windows)
      BuildCommands.setBuildCommandState(
            RStudioGinjector.INSTANCE.getCommands(), 
            RStudioGinjector.INSTANCE.getSession().getSessionInfo());
      
      // initialize MRUs on Cocoa desktop (on Qt and in server mode the MRU
      // menu entries aren't accessible from the source window)
      if (Desktop.isDesktop() && Desktop.getFrame().isCocoa())
      {
         pFileMRUList_.get();
         pProjectMRUList_.get();
      }
      
      // make it fill the containing layout panel
      Widget presWidget = appPresenter.asWidget();
      mainPanel.add(presWidget);
      mainPanel.setWidgetLeftRight(presWidget, 0, Unit.PX, 0, Unit.PX);
      mainPanel.setWidgetTopBottom(presWidget, 0, Unit.PX, 0, Unit.PX);
   }

   @Override
   public void reactivate(JavaScriptObject params)
   {
   }
   
   @Override 
   public Widget getWidget()
   {
      return this;
   }
   
   private final Provider<SourceSatellitePresenter> pPresenter_;
   private final Provider<SourceWindowManager> pWindowManager_;
   private final Provider<SourceWindow> pSourceWindow_;
   private final Provider<FileMRUList> pFileMRUList_;
   private final Provider<ProjectMRUList> pProjectMRUList_;
}