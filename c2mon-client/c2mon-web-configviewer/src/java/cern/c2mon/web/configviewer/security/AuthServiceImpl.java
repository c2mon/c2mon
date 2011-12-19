package cern.c2mon.web.configviewer.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements UserDetailsService  {

  @Override
  public UserDetails loadUserByUsername(String arg0) throws UsernameNotFoundException, DataAccessException {
    // TODO Auto-generated method stub
    return null;
  }
}