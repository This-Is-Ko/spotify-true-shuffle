package com.example.shuffle.controller;

import com.example.shuffle.auth.AuthCodeRedirectResponse;
import com.example.shuffle.auth.AuthCodeResponse;
import com.example.shuffle.config.SpotifyConfigProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;

@RestController
@ResponseBody
@CrossOrigin(origins = {"http://localhost:3000","https://spotify-true-shuffle-react.vercel.app"})
@RequestMapping("/auth/spotify")
public class SpotifyAuthController {

    Logger LOG = LoggerFactory.getLogger(SpotifyAuthController.class);

    @Autowired
    private SpotifyConfigProperties spotifyConfigProperties;
    private SpotifyApi spotifyApi;

    // Run after construction of instance so config props can be retrieved
    @PostConstruct
    public void init() {
        LOG.debug(spotifyConfigProperties.getClientId());
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyConfigProperties.getClientId())
                .setClientSecret(spotifyConfigProperties.getClientSecret())
                .setRedirectUri(SpotifyHttpManager.makeUri(spotifyConfigProperties.getRedirectUri()))
                .build();
    }

    @GetMapping(value = "/auth-login")
    public @ResponseBody AuthCodeResponse generateSpotifyLoginUri() {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("playlist-modify-private,playlist-modify-public,playlist-read-private,playlist-read-collaborative,user-library-read")
                .state(RandomStringUtils.randomAlphanumeric(16))
                .build();

        final URI uri = authorizationCodeUriRequest.execute();
        LOG.info("Successfully created spotify auth url");
        return new AuthCodeResponse(uri.toString());
    }

    @GetMapping(value = "/handle-auth-code")
    public @ResponseBody AuthCodeRedirectResponse handleAuthCode(@RequestParam String code) {
        try{
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
                    .build();

            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            LOG.info("Access token obtained succesfully through auth code");
            return new AuthCodeRedirectResponse("Success", authorizationCodeCredentials.getAccessToken(), authorizationCodeCredentials.getRefreshToken(), authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e){
            System.out.println("Error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/handle-refresh-token")
    public @ResponseBody AuthCodeRedirectResponse handleRefreshToken(@RequestParam String refreshToken) {
        try{
            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                    .grant_type("refresh_token")
                    .refresh_token(refreshToken)
                    .build();

            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            LOG.info("New access token obtained succesfully through refresh token");
            return new AuthCodeRedirectResponse("Success", authorizationCodeCredentials.getAccessToken(), authorizationCodeCredentials.getRefreshToken(), authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e){
            System.out.println("Error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
