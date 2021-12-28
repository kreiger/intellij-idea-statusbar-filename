package com.linuxgods.kreiger.idea;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.impl.EditorTabPresentationUtil;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.VfsPresentationUtil;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.Consumer;
import com.intellij.util.IconUtil;
import com.intellij.util.SlowOperations;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

class FileNameStatusBarWidget extends EditorBasedWidget implements StatusBarWidget.MultipleTextValuesPresentation {
    private String text;
    private Icon icon;

    public FileNameStatusBarWidget(@NotNull Project project) {
        super(project);
    }

    @Override public void dispose() {
        super.dispose();
    }

    @Override public @NonNls @NotNull String ID() {
        return "Filename";
    }

    @Override public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return null;
    }

    @Override public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        DumbService.getInstance(myProject).runWhenSmart(() -> update(getSelectedFile()));
    }

    @Override public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        update(event.getNewFile());
    }


    private void update(VirtualFile file) {
        if (null == file) return;
        text = getFileTitle(file);
        icon = IconUtil.getIcon(file, 0, myProject);
        myStatusBar.updateWidget(ID());
    }

    private String getFileTitle(VirtualFile file) {
        return SlowOperations.allowSlowOperations(() -> VfsPresentationUtil.getUniquePresentableNameForUI(myProject, file));
    }

    @Override public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
        return new ListPopupImpl(myProject, new RecentFilesPopupStep(myProject));
    }

    @Override public @Nullable @NlsContexts.StatusBarText String getSelectedValue() {
        return text;
    }

    @Override public @Nullable Icon getIcon() {
        return icon;
    }

    private static List<VirtualFile> getSelectionHistory(FileEditorManagerImpl fileEditorManager) {
        List<VirtualFile> selectionHistory = fileEditorManager.getSelectionHistory().stream()
                .map(pair -> pair.getFirst())
                .collect(toList());
        Collections.reverse(selectionHistory);
        return selectionHistory;
    }

    private class RecentFilesPopupStep extends BaseListPopupStep<VirtualFile> {
        private final FileEditorManager fileEditorManager;

        public RecentFilesPopupStep(Project project) {
            this((FileEditorManagerImpl) FileEditorManagerImpl.getInstance(project));
        }

        private RecentFilesPopupStep(FileEditorManagerImpl fileEditorManager) {
            super(IdeBundle.message("title.popup.recent.files"), getSelectionHistory(fileEditorManager));
            this.fileEditorManager = fileEditorManager;
        }

        @Override public Icon getIconFor(VirtualFile file) {
            return IconUtil.getIcon(file, 0, myProject);
        }

        @Override public @NotNull String getTextFor(VirtualFile file) {
            return getFileTitle(file);
        }

        @Override
        public @Nullable PopupStep<?> onChosen(VirtualFile file, boolean finalChoice) {
            fileEditorManager.openFile(file, true);
            return FINAL_CHOICE;
        }
    }
}
