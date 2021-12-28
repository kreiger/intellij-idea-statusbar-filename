package com.linuxgods.kreiger.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class FileNameStatusBarWidgetFactory extends StatusBarEditorBasedWidgetFactory {
    @Override public @NonNls @NotNull String getId() {
        return "FileName";
    }

    @Override public @Nls @NotNull String getDisplayName() {
        return "File Name";
    }

    @Override public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new FileNameStatusBarWidget(project);
    }

    @Override public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }
}
