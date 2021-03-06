package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import com.kylenicholls.stash.parameterizedbuilds.item.UserToken;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class JenkinsTest {
    private static final String PLUGIN_KEY = "com.kylenicholls.stash.parameterized-builds";
    private static final String USER_SLUG = "slug";
    private static final String PROJECT_KEY = "projkey";
    private Jenkins jenkins;
    private PluginSettings pluginSettings;
    private ApplicationUser user;
    private Project project;
    private ProjectService projectService;

    @Before
    public void setup() throws IOException {
        PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
        pluginSettings = mock(PluginSettings.class);
        when(factory.createSettingsForKey(PLUGIN_KEY)).thenReturn(pluginSettings);
        jenkins = new Jenkins(factory);

        user = mock(ApplicationUser.class);
        when(user.getSlug()).thenReturn(USER_SLUG);
        project = mock(Project.class);
        when(project.getKey()).thenReturn(PROJECT_KEY);
        projectService = mock(ProjectService.class);
    }

    @Test
    public void testSaveGlobalJenkinsServerNull() {
        jenkins.saveJenkinsServer(null, null);

        verify(pluginSettings, times(1)).remove(".jenkinsSettings");
    }

    @Test
    public void testSaveProjectJenkinsServerNull() {
        jenkins.saveJenkinsServer(null, PROJECT_KEY);

        verify(pluginSettings, times(1)).remove(".jenkinsSettings." + PROJECT_KEY);
    }

    @Test
    public void testSaveGlobalJenkinsServer() {
        Server server = new Server("url", null, "user", "token", true, true);
        jenkins.saveJenkinsServer(server, null);

        verify(pluginSettings, times(1)).put(".jenkinsSettings", server.asMap());
    }

    @Test
    public void testSaveProjectJenkinsServer() {
        Server server = new Server("url", null, "user", "token", true, true);
        jenkins.saveJenkinsServer(server, PROJECT_KEY);

        verify(pluginSettings, times(1)).put(".jenkinsSettings." + PROJECT_KEY, server.asMap());
    }

    @Test
    public void testSaveGlobalUserTokenWithEmptyToken() {
        jenkins.saveUserToken(USER_SLUG, "", "");

        verify(pluginSettings, times(1)).remove(".jenkinsUser." + USER_SLUG);
    }

    @Test
    public void testSaveProjectUserTokenWithEmptyToken() {
        jenkins.saveUserToken(USER_SLUG, PROJECT_KEY, "");

        verify(pluginSettings, times(1)).remove(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY);
    }

    @Test
    public void testSaveGlobalUserToken() {
        String token = "token";
        jenkins.saveUserToken(USER_SLUG, "", token);

        verify(pluginSettings, times(1)).put(".jenkinsUser." + USER_SLUG, token);
    }

    @Test
    public void testSaveProjectUserToken() {
        String token = "token";
        jenkins.saveUserToken(USER_SLUG, PROJECT_KEY, token);

        verify(pluginSettings, times(1))
                .put(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY, token);
    }

    @Test
    public void testGetJenkinsServerSettingsNull() {
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(null);
        Server actual = jenkins.getJenkinsServer(null, null);

        assertEquals(null, actual);
    }

    @Test
    public void testGetJenkinsServerSettingsLegacyNoAltUrl() {
        when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token");
        Server actual = jenkins.getJenkinsServer(null, null);

        assertEquals("url", actual.getBaseUrl());
        assertEquals("user", actual.getUser());
        assertEquals("token", actual.getToken());
        assertFalse(actual.getAltUrl());
    }

    @Test
    public void testGetJenkinsServerSettingsLegacyAltUrlTrue() {
        when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token;true");
        Server actual = jenkins.getJenkinsServer(null, null);

        assertEquals("url", actual.getBaseUrl());
        assertEquals("user", actual.getUser());
        assertEquals("token", actual.getToken());
        assertTrue(actual.getAltUrl());
    }

    @Test
    public void testGetJenkinsServerSettingsLegacyAltUrlFalse() {
        when(pluginSettings.get(".jenkinsSettings")).thenReturn("url;user;token;false");
        Server actual = jenkins.getJenkinsServer(null, null);

        assertEquals("url", actual.getBaseUrl());
        assertEquals("user", actual.getUser());
        assertEquals("token", actual.getToken());
        assertFalse(actual.getAltUrl());
    }

    @Test
    public void testGetJenkinsServerSettings() {
        Server expected = new Server("url", null, "user", "token", false, false);
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(expected.asMap());
        Server actual = jenkins.getJenkinsServer(null, null);

        assertEquals(expected.asMap(), actual.asMap());
    }

    @Test
    public void testGetProjectServerSettingsNull() {
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(null);
        Server actual = jenkins.getJenkinsServer(PROJECT_KEY, null);

        assertEquals(null, actual);
    }

    @Test
    public void testGetProjectServerSettings() {
        Server expected = new Server("url", null, "user", "token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
        Server actual = jenkins.getJenkinsServer(PROJECT_KEY, null);

        assertEquals(expected.asMap(), actual.asMap());
    }

    @Test
    public void testGetAllUserTokensWithGlobalServer() {
        Server globalServer = new Server("http://globalUrl", null, "globaluser", "globaltoken",
                false, false);
        String token = "token";
        List<String> projectKeys = new ArrayList<>();
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(globalServer.asMap());
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn(token);
        List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

        assertEquals(1, actual.size());
        assertEquals(globalServer.getBaseUrl(), actual.get(0).getBaseUrl());
        assertEquals("", actual.get(0).getProjectKey());
        assertEquals("Global", actual.get(0).getProjectName());
        assertEquals(USER_SLUG, actual.get(0).getUserSlug());
        assertEquals(token, actual.get(0).getToken());
    }

    @Test
    public void testGetAllUserTokensWithGlobalServerNull() {
        List<String> projectKeys = new ArrayList<>();
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(null);
        List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

        assertEquals(0, actual.size());
    }

    @Test
    public void testGetAllUserTokensProjectServerNull() {
        Server globalServer = new Server("http://globalUrl", null, "globaluser", "globaltoken",
                false, false);
        String newProjectKey = "newkey";
        List<String> projectKeys = new ArrayList<>();
        projectKeys.add(newProjectKey);
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(globalServer.asMap());
        when(pluginSettings.get(".jenkinsSettings." + newProjectKey)).thenReturn(null);
        List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

        assertEquals(1, actual.size());
    }

    @Test
    public void testGetAllUserTokensProjectServerTokenNull() {
        Server globalServer = new Server("http://globalUrl", null, "globaluser", "globaltoken",
                false, false);
        String newProjectKey = "newkey";
        String newProjectName = "newName";
        List<String> projectKeys = new ArrayList<>();
        projectKeys.add(newProjectKey);
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(globalServer.asMap());
        when(projectService.getByKey(newProjectKey)).thenReturn(project);
        when(project.getName()).thenReturn(newProjectName);
        Server projectServer = new Server("http://newbaseurl", null, "newuser", "newtoken", false,
                false);
        when(pluginSettings.get(".jenkinsSettings." + newProjectKey))
                .thenReturn(projectServer.asMap());
        List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

        assertEquals(2, actual.size());
        assertEquals(projectServer.getBaseUrl(), actual.get(0).getBaseUrl());
        assertEquals(newProjectKey, actual.get(0).getProjectKey());
        assertEquals(newProjectName, actual.get(0).getProjectName());
        assertEquals(USER_SLUG, actual.get(0).getUserSlug());
        assertEquals(null, actual.get(0).getToken());
    }

    @Test
    public void testGetAllUserTokensProjectServer() {
        Server globalServer = new Server("http://globalUrl", null, "globaluser", "globaltoken",
                false, false);
        String newProjectKey = "newkey";
        String newProjectName = "newName";
        String token = "token";
        List<String> projectKeys = new ArrayList<>();
        projectKeys.add(newProjectKey);
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(globalServer.asMap());
        when(projectService.getByKey(newProjectKey)).thenReturn(project);
        when(project.getName()).thenReturn(newProjectName);
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + newProjectKey))
                .thenReturn(token);
        Server projectServer = new Server("http://newbaseurl", null, "newuser", "newtoken", false,
                false);
        when(pluginSettings.get(".jenkinsSettings." + newProjectKey))
                .thenReturn(projectServer.asMap());
        List<UserToken> actual = jenkins.getAllUserTokens(user, projectKeys, projectService);

        assertEquals(2, actual.size());
        assertEquals(token, actual.get(0).getToken());
    }

    @Test
    public void testGetJoinedGlobalToken() {
        String token = "token";
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn("token");

        Server expected = new Server("url", null, "default_user", "default_token", false, false);
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(expected.asMap());

        Server actual = jenkins.getJenkinsServer(null, null, user);
        assertEquals(user.getSlug(), actual.getUser());
        assertEquals(token, actual.getToken());
    }

    @Test
    public void testGetServerWithoutUserGlobalToken() {
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn(null);

        Server expected = new Server("url", null, "default_user", "default_token", false, false);
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(expected.asMap());

        Server actual = jenkins.getJenkinsServer(null, null, user);
        assertEquals("default_user", actual.getUser());
        assertEquals("default_token", actual.getToken());
    }

    @Test
    public void testGetServerGlobalTokenNullUser() {
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn(null);

        Server expected = new Server("url", null, "default_user", "default_token", false, false);
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(expected.asMap());

        Server actual = jenkins.getJenkinsServer(null, null, null);
        assertEquals("default_user", actual.getUser());
        assertEquals("default_token", actual.getToken());
    }

    @Test
    public void testGetServerWithUserProjectToken() {
        String token = "token";
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY))
                .thenReturn("token");

        Server expected = new Server("url", null, "default_user", "default_token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());

        Server actual = jenkins.getJenkinsServer(PROJECT_KEY, null, user);
        assertEquals(user.getSlug(), actual.getUser());
        assertEquals(token, actual.getToken());
    }

    @Test
    public void testGetServerWithoutUserProjectToken() {
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn(null);

        Server expected = new Server("url", null, "default_user", "default_token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());

        Server actual = jenkins.getJenkinsServer(PROJECT_KEY, null, user);
        assertEquals("default_user", actual.getUser());
        assertEquals("default_token", actual.getToken());
    }

    @Test
    public void testGetServerProjectTokenNullUser() {
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn(null);

        Server expected = new Server("url", null, "default_user", "default_token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());

        Server actual = jenkins.getJenkinsServer(PROJECT_KEY, null, null);
        assertEquals("default_user", actual.getUser());
        assertEquals("default_token", actual.getToken());
    }
}
