
default: install

java: jopenray/build.xml
	( cd jopenray && ant )

dist:
	mkdir -pv dist

dist/kOpenRay/README.md: dist
	cp -R assets/ dist/kOpenRay

install: dist/kOpenRay/README.md java
	cp jopenray/dist/kOpenRay.jar dist/kOpenRay

clean:
	rm -rf dist jopenray/build jopenray/dist
