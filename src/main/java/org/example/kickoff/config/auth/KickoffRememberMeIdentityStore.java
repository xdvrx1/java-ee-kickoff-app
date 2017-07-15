package org.example.kickoff.config.auth;

import static java.util.logging.Level.WARNING;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static org.example.kickoff.model.LoginToken.TokenType.REMEMBER_ME;
import static org.omnifaces.util.Servlets.getRemoteAddr;

import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.credential.RememberMeCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.RememberMeIdentityStore;
import javax.servlet.http.HttpServletRequest;

import org.example.kickoff.business.service.LoginTokenService;
import org.example.kickoff.business.service.UserService;
import org.example.kickoff.model.User;

@ApplicationScoped
public class KickoffRememberMeIdentityStore implements RememberMeIdentityStore {

	private static final Logger logger = Logger.getLogger(KickoffRememberMeIdentityStore.class.getName());

	@Inject
	private HttpServletRequest request;

	@Inject
	private UserService userService;

	@Inject
	private LoginTokenService loginTokenService;

	@Override
	public CredentialValidationResult validate(RememberMeCredential credential) {
		try {
			User user = userService.getByLoginToken(credential.getToken(), REMEMBER_ME);

			if (user != null) {
				return new CredentialValidationResult(new CallerPrincipal(user.getEmail()), user.getRolesAsStrings());
			}
		}
		catch (Exception e) {
			logger.log(WARNING, "Unable to authenticate user using token.", e);
		}

		return INVALID_RESULT;
	}

	@Override
	public String generateLoginToken(CallerPrincipal callerPrincipal, Set<String> groups) {
		return loginTokenService.generate(callerPrincipal.getName(), getRemoteAddr(request), getDescription(), REMEMBER_ME);
	}

	@Override
	public void removeLoginToken(String loginToken) {
		if (loginToken != null) {
			loginTokenService.remove(loginToken);
		}
	}

	private String getDescription() {
		return "Remember me session: " + request.getHeader("User-Agent");
	}

}