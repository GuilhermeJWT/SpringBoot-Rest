package spring.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import spring.rest.ApplicationContextLoad;
import spring.rest.model.Usuario;
import spring.rest.repository.UsuarioRepository;

@Service
@Component
public class JWTTokenAutenticacaoService {

	private static final long EXPIRATION_TIME = 900000000;

	private static final String SECRET = "SenhaExtremamenteSecreta";

	private static final String TOKEN_PREFIX = "Bearer";

	private static final String HEADER_STRING = "Authorization";

	public void addAuthentication(HttpServletResponse response, String username) throws IOException {

		/* Montagem do Token */
		String JWT = Jwts.builder() 
				.setSubject(username) 
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();


		String token = TOKEN_PREFIX + " " + JWT;

		/* Adiciona no cabeçalho http */
		response.addHeader(HEADER_STRING, token);

		ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
		.atualizaTokenUser(JWT, username);;

		liberacaoCors(response);

		response.getWriter().write("{\"Authorization\": \"" + token + "\"}");

	}

	public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {

		/* Pega o Token enviado no cabeçalho Http */
		String token = request.getHeader(HEADER_STRING);

		try {
			if (token != null) {

				String tokemLimpo = token.replace(TOKEN_PREFIX, "").trim();

				/* Faz a validação do token do usuario na requisição */
				String user = Jwts.parser().setSigningKey(SECRET) 
						.parseClaimsJws(tokemLimpo)
						.getBody().getSubject(); /* Guilherme Santos - com seus acessos*/

				if (user != null) {

					Usuario usuario = ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
							.findUserByLogin(user);

					if (usuario != null) {

						if (tokemLimpo.equalsIgnoreCase(usuario.getToken())) {

							return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(),
									usuario.getAuthorities());
						}
					}

				}
			}
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
				try {
					response.getOutputStream().println("Seu Token está Expirado, faça o Login Novamente ou Informe um novo Token para Autenticação!");
				} catch (IOException e1) {
					
				}
		}

		liberacaoCors(response);

		/* o Token não foi autorizado */
		return null;

	}

	/*Nessa parte precisei implementar a liberação de Cors, pois os navegadores desabilitam por padrão*/

	private void liberacaoCors(HttpServletResponse response) {

		if (response.getHeader("Access-Control-Allow-Origin") == null) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}
		if (response.getHeader("Access-Control-Allow-Headers") == null) {
			response.addHeader("Access-Control-Allow-Headers", "*");
		}
		if (response.getHeader("Access-Control-Request-Headers") == null) {
			response.addHeader("Access-Control-Request-Headers", "*");
		}
		if (response.getHeader("Access-Control-Allow-Methods") == null) {
			response.addHeader("Access-Control-Allow-Methods", "*");
		}
	}

}
