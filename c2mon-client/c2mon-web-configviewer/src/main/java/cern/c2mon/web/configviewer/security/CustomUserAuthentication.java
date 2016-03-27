///******************************************************************************
// * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
// *
// * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
// * C2MON is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the license.
// *
// * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// * more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
// *****************************************************************************/
//package cern.c2mon.web.configviewer.security;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//public class CustomUserAuthentication implements Authentication {
//
//    private static final long serialVersionUID = -3091441742758356129L;
//
//    private boolean authenticated;
//
//    private GrantedAuthority grantedAuthority;
//    private Authentication authentication;
//
//    public CustomUserAuthentication(String role, Authentication authentication) {
//        this.grantedAuthority = new SimpleGrantedAuthority(role);
//        this.authentication = authentication;
//    }
//
//    @Override
//    public Collection<GrantedAuthority> getAuthorities() {
//        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
//        authorities.add(grantedAuthority);
//        return authorities;
//    }
//
//    @Override
//    public Object getCredentials() {
//        return authentication.getCredentials();
//    }
//
//    @Override
//    public Object getDetails() {
//        return authentication.getDetails();
//    }
//
//    @Override
//    public Object getPrincipal() {
//        return authentication.getPrincipal();
//    }
//
//    @Override
//    public boolean isAuthenticated() {
//        return authenticated;
//    }
//
//    @Override
//    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
//        this.authenticated = authenticated;
//    }
//
//    @Override
//    public String getName() {
//        return (String) authentication.getPrincipal();
//    }
//}
