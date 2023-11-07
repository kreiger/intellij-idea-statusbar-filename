package com.linuxgods.kreiger.idea;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.ListSeparator;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.VfsPresentationUtil;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class FileNameStatusBarWidget extends EditorBasedStatusBarPopup {
    private String text;
    private Icon icon;

    public FileNameStatusBarWidget(@NotNull Project project) {
        super(project, false);
    }

    @Override public @NonNls @NotNull String ID() {
        return "Filename";
    }

    @Override public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
    }

    @Override public void handleFileChange(VirtualFile file) {
        update(file);
    }

    private void update(VirtualFile file) {
        if (null == file) return;
        text = getFileTitle(file);
        icon = IconUtil.computeFileIcon(file, 0, getProject());
    }

    private String getFileTitle(VirtualFile file) {
        return VfsPresentationUtil.getUniquePresentableNameForUI(getProject(), file);
    }

    @NotNull @Override protected StatusBarWidget createInstance(@NotNull Project project) {
        return new FileNameStatusBarWidget(project);
    }

    @Nullable @Override protected ListPopup createPopup(@NotNull DataContext dataContext) {
        List<VirtualFile> recentFiles = EditorHistoryManager.getInstance(getProject()).getFileList();
        if (recentFiles.isEmpty()) return null;
        return new ListPopupImpl(getProject(), new RecentFilesPopupStep(recentFiles));
    }

    @NotNull @Override protected WidgetState getWidgetState(@Nullable VirtualFile file) {
        if (file == null) return WidgetState.HIDDEN;
        WidgetState widgetState = new WidgetState(text, text, true);
        widgetState.setIcon(icon);
        return widgetState;
    }

    private class RecentFilesPopupStep extends BaseListPopupStep<VirtualFile> {
        public RecentFilesPopupStep(List<VirtualFile> files) {
            super(IdeBundle.message("title.popup.recent.files"), files);
            setDefaultOptionIndex(files.size() - 1);
        }

        @Override public Icon getIconFor(VirtualFile file) {
            if (file == null) return null;
            return IconUtil.getIcon(file, Iconable.ICON_FLAG_READ_STATUS, getProject());
        }

        @Override public @Nullable Color getForegroundFor(VirtualFile file) {
            if (file == null) return null;
            return FileStatusManager.getInstance(getProject()).getStatus(file).getColor();
        }

        @Override public @NotNull String getTextFor(VirtualFile file) {
            if (file == null) return "";
            return getFileTitle(file);
        }

        @Override
        public @Nullable PopupStep<?> onChosen(VirtualFile file, boolean finalChoice) {
            if (file != null && finalChoice) FileEditorManager.getInstance(getProject()).openFile(file, true);
            return FINAL_CHOICE;
        }

        @Override public @Nullable ListSeparator getSeparatorAbove(VirtualFile value) {
            List<VirtualFile> values = getValues();
            if (values.isEmpty()) return null;
            VirtualFile mostRecentFile = values.get(values.size() - 1);
            if (!value.equals(mostRecentFile)) return null;
            return new ListSeparator(IdeBundle.message("scope.current.file"));
        }
    }
}
