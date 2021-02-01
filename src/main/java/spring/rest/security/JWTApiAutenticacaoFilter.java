package spring.rest.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/*Parte onde as nossas requisições vão ser pegas e serão autenticadas exemplo /guilherme/edit, 
tudo vai ser valdiado e autenticado antes de realizar qualquer processo*/
public class JWTApiAutenticacaoFilter extends GenericFilterBean{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		Authentication authentication = new JWTTokenAutenticacaoService()
				.getAuthentication((HttpServletRequest)request, (HttpServletResponse) response);
		
		/*Coloca o processo de autenticacao no spring security*/
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		chain.doFilter(request, response);
		
		
	}

}
