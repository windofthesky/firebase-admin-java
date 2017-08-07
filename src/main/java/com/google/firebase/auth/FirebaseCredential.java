/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.auth;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableList;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Provides Google OAuth2 access tokens used to authenticate with Firebase services. In most cases,
 * you will not need to implement this yourself and can instead use the default implementations
 * provided by {@link FirebaseCredentials}.
 */
public abstract class FirebaseCredential {

  private static final List<String> FIREBASE_SCOPES =
      ImmutableList.of(
          "https://www.googleapis.com/auth/firebase.database",
          "https://www.googleapis.com/auth/userinfo.email",
          "https://www.googleapis.com/auth/identitytoolkit");

  private final GoogleCredentials googleCredentials;

  public FirebaseCredential(GoogleCredentials googleCredentials) {
    this.googleCredentials = checkNotNull(googleCredentials).createScoped(FIREBASE_SCOPES);
  }

  public final GoogleCredentials getGoogleCredentials() {
    return googleCredentials;
  }

  /**
   * Returns a Google OAuth2 access token which can be used to authenticate with Firebase services.
   * This method does not cache tokens, and therefore each invocation will fetch a fresh token.
   * The caller is expected to implement caching by referencing the token expiry details
   * available in the returned GoogleOAuthAccessToken instance.
   *
   * @return A {@link Task} providing a Google OAuth access token.
   */
  public final Task<GoogleOAuthAccessToken> getAccessToken() {
    return Tasks.call(new Callable<GoogleOAuthAccessToken>() {
      @Override
      public GoogleOAuthAccessToken call() throws Exception {
        AccessToken accessToken = googleCredentials.refreshAccessToken();
        checkNotNull(accessToken);
        return new GoogleOAuthAccessToken(accessToken.getTokenValue(),
            accessToken.getExpirationTime().getTime());
      }
    });
  }
}
