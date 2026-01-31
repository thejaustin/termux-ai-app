package com.termux.plus.plugin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;

import com.termux.plus.api.TermuxPlugin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PluginManagerTest {

    @Mock
    Context context;
    
    @Mock
    SharedPreferences prefs;
    
    @Mock
    SharedPreferences.Editor editor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getApplicationContext()).thenReturn(context);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
        when(prefs.edit()).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
    }

    @Test
    public void testRegisterPlugin() {
        // PluginManager instance calls getSharedPreferences in constructor
        PluginManager manager = PluginManager.getInstance(context);
        
        TermuxPlugin mockPlugin = mock(TermuxPlugin.class);
        when(mockPlugin.getId()).thenReturn("test.plugin");
        when(mockPlugin.getName()).thenReturn("Test Plugin");
        when(prefs.getBoolean("test.plugin", true)).thenReturn(true);

        manager.registerPlugin(mockPlugin);

        assertNotNull(manager.getPlugin("test.plugin"));
        verify(mockPlugin).onInit(any());
    }
}
