Name: @NAME@
Summary: @SUMMARY@
Version: @VERSION@
Release: @RELEASE@
License: Apache2
Group: Quattor
URL: http://quattor.org/
BuildRoot: @BUILDROOT@
BuildArch: noarch
Source: @TARFILE@
# Unfortunately the Sun packages don't provide reasonable version
# numbers or even descent tags.   Can't use rpm to check for broken 
# dependencies.  
#Requires: jre >= 1.5

%description
Quattor toolkit compiler for the pan configuration language.

%prep
%setup -q

%build

%install
[ -d $RPM_BUILD_ROOT ] && rm -fr $RPM_BUILD_ROOT
mkdir -p %{buildroot}/usr/
mkdir -p %{buildroot}/usr/share/doc/panc-%{version}/
mkdir -p %{buildroot}/usr/share/man/
cp -r bin/ %{buildroot}/usr/
cp -r lib/ %{buildroot}/usr/
cp -r doc/* %{buildroot}/usr/share/doc/panc-%{version}/
cp -r man/* %{buildroot}/usr/share/man

%clean
rm -fr $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
/usr/bin
/usr/lib
%doc /usr/share/man
%doc /usr/share/doc/panc-%{version}/
