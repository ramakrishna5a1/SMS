package com.messages.smsmessagesimporter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.messages.smsmessagesimporter.DataHandler.DataProcessedCallback;
import com.messages.smsmessagesimporter.DataHandler.JSONDataParser;
import com.messages.smsmessagesimporter.DataHandler.DataUtils;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JSONDataParserTest {

    @Mock
    private Activity mockActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRun_SuccessfulParsing() throws JSONException, InterruptedException {
        DataUtils mockDataUtils = mock(DataUtils.class);
        when(mockDataUtils.getJsonString()).thenReturn(getMockJsonString());

        DataProcessedCallback mockCallback = mock(DataProcessedCallback.class);

        JSONDataParser jsonDataParser = new JSONDataParser(mockActivity);
        //jsonDataParser.setCallback(mockCallback);

        CountDownLatch latch = new CountDownLatch(1);

        new Handler(Looper.getMainLooper()).post(() -> {
            jsonDataParser.run();
            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);

        verify(mockDataUtils).setJsonParseSuccess(true);
        //verify(mockCallback).onJSONDataProcessed();
    }

    private String getMockJsonString() {
        // Implement mock JSON string
        return "[{\"address\":\"1234567890\",\"body\":\"Test message 1\",\"date\":\"01/01/2024 10:00:00 AM\"}," +
                "{\"address\":\"0987654321\",\"body\":\"Test message 2\",\"date\":\"02/01/2024 11:00:00 AM\"}]";
    }}
