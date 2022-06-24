package com.example.shuffle.controller;

import com.example.shuffle.auth.AuthCodeRedirectResponse;
import com.example.shuffle.auth.AuthCodeResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.core5.http.ParseException;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.IOException;
import java.net.URI;

@RestController
@ResponseBody
@RequestMapping("/auth/spotify")
public class SpotifyAuthController {

    SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId("***")
            .setClientSecret("***")
            .setRedirectUri(SpotifyHttpManager.makeUri("http://localhost:8080/auth/spotify/handle-auth-code/"))
            .build();

    @GetMapping(value = "/auth-login")
    public @ResponseBody AuthCodeResponse generateSpotifyLoginUri() {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("playlist-modify-private,playlist-modify-public,playlist-read-private,playlist-read-collaborative")
                .state(RandomStringUtils.randomAlphanumeric(16))
                .build();

        final URI uri = authorizationCodeUriRequest.execute();
        return new AuthCodeResponse(uri.toString());
    }

    @GetMapping(value = "/handle-auth-code")
    public @ResponseBody AuthCodeRedirectResponse handleAuthCodeRedirect(@RequestParam String code) {
        System.out.println(code);
        try{
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
                    .build();

            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            return new AuthCodeRedirectResponse("Success", authorizationCodeCredentials.getAccessToken(), authorizationCodeCredentials.getRefreshToken(), authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e){
            System.out.println("Error: " + e.getMessage());
            return new AuthCodeRedirectResponse("Error", null, null, null);
        }
    }
}