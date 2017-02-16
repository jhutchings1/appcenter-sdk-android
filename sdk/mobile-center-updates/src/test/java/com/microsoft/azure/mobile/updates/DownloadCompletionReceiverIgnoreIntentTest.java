package com.microsoft.azure.mobile.updates;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Updates.class)
public class DownloadCompletionReceiverIgnoreIntentTest {

    @Test
    public void invalidIntent() {
        mockStatic(Updates.class);
        when(Updates.getInstance()).thenReturn(mock(Updates.class));
        Intent clickIntent = mock(Intent.class);
        when(clickIntent.getAction()).thenReturn(Intent.ACTION_ANSWER);
        new DownloadCompletionReceiver().onReceive(mock(Context.class), clickIntent);
        when(clickIntent.getAction()).thenReturn(null);
        new DownloadCompletionReceiver().onReceive(mock(Context.class), clickIntent);
        verifyStatic(never());
        Updates.getInstance();
    }
}
