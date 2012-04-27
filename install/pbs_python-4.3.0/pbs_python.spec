%{!?python_sitearch: %define python_sitearch %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib(1)")}

### Abstract ###

Name: pbs_python
Version: 4.3.0
Release: 1%{?dist}
License: See LICENSE
Group: Development/Libraries
Summary: This package contains the PBS python module.
URL: https://subtrac.sara.nl/oss/pbs_python
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root
Source: ftp://ftp.sara.nl/pub/outgoing/pbs_python.tar.gz 

### Dependencies ###
# None

### Build Dependencies ###

BuildRequires: libtorque-devel >= %{libtorque_version}
BuildRequires: python2-devel >= %{python_version}

%description
This package contains the pbs python module.

%prep
%setup -q -n pbs_python-%{version}

%build
%configure
%{__python} setup.py build

%install
%{__python} ./setup.py install --prefix $RPM_BUILD_ROOT%{_prefix} ;

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,0755)
%doc README TODO examples
%{python_sitearch}/pbs.pth
%{python_sitearch}/pbs/*

%changelog
* Tue Mar 24 2010 Ramon Bastiaans <ramon.bastiaans@sara.nl>
- Updates for new version
* Tue Oct 06 2009 Ramon Bastiaans <ramon.bastiaans@sara.nl>
- Fixed tmppath, %setup sourcedir
* Tue Mar 24 2009 David Chin <chindw@wfu.edu>
- Fedora-ize
* Sun Mar  9 2008 Michael Sternberg <sternberg@anl.gov>
- libdir and python defines
* Wed Nov 23 2005 Ramon Bastiaans <bastiaans@sara.nl>
- Fixed missing prep setup and added configure
* Tue Nov 22 2005 Martin Pels <pels@sara.nl>
- Changed default directory permissions
* Tue Nov 01 2005 Martin Pels <pels@sara.nl> 
- Initial version

