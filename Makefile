# Shortcuts for the fork's common loops. Run `make` (or `make help`) to list them.
#
#   make run-tv                     # build signed release, install on the living-room TV, launch
#   make run-tv TV=192.168.0.73     # override the TV from local.mk
#   make test                       # JVM unit tests, no device needed

# Per-machine settings (TV=...), gitignored. Copy local.mk.example to local.mk.
-include local.mk
# Signing secrets (BEEB_KEYSTORE_PATH, BEEB_KEYSTORE_PASSWORD, BEEB_KEY_ALIAS), gitignored.
# Gradle's localRelease signing config reads them from the environment.
-include .env
export BEEB_KEYSTORE_PATH BEEB_KEYSTORE_PASSWORD BEEB_KEY_ALIAS

ANDROID_HOME ?= $(HOME)/Android/Sdk
ADB          := $(ANDROID_HOME)/platform-tools/adb
GRADLE       := ./gradlew --console=plain
APP_ID       := org.beebplayer.android.jon
DEBUG_ID     := $(APP_ID).debug
# Kotlin packages are upstream's, so the activity class keeps the upstream package.
ACTIVITY      = $(1)/org.beebplayer.android.MainActivity
RELEASE_APK  := app/build/outputs/apk/release/app-release.apk
DEBUG_APK    := app/build/outputs/apk/debug/app-debug.apk
COMPILE_SDK  := android-37.0
TV           ?=
WIFI_DEVICE  := ADB=$(ADB) scripts/wifi-device.sh

ifdef DEVICE
export ANDROID_SERIAL := $(DEVICE)
endif

.DEFAULT_GOAL := help

.PHONY: help
help: ## List the available targets
	@grep -hE '^[a-zA-Z_-]+:.*## |^##@' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*## "} \
			/^##@/ {printf "\n\033[1m%s\033[0m\n", substr($$0, 5); next} \
			{printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

##@ 1. Setup

.PHONY: doctor
doctor: ## Check JDK, SDK platform 37, local.properties, signing .env and the TV setting
	@if command -v javac >/dev/null; then javac -version; else echo "!!  no javac: install a JDK 17+"; fi
	@if [ -x $(ADB) ]; then echo "ok  $(ADB)"; else echo "!!  missing $(ADB)"; fi
	@if [ -d $(ANDROID_HOME)/platforms/$(COMPILE_SDK) ]; then echo "ok  SDK $(COMPILE_SDK)"; \
		else echo "!!  missing SDK $(COMPILE_SDK): $(ANDROID_HOME)/cmdline-tools/latest/bin/android sdk install platforms/$(COMPILE_SDK)"; fi
	@if [ -f local.properties ]; then echo "ok  local.properties"; else echo "!!  missing local.properties: make local-properties"; fi
	@if [ -n "$(BEEB_KEYSTORE_PATH)" ] && [ -f "$(BEEB_KEYSTORE_PATH)" ]; then echo "ok  keystore $(BEEB_KEYSTORE_PATH)"; \
		else echo "!!  no release keystore: make keystore (see README)"; fi
	@if [ -n "$(TV)" ]; then echo "ok  TV=$(TV)"; else echo "--  TV unset: copy local.mk.example to local.mk"; fi
	@git remote get-url upstream >/dev/null 2>&1 && echo "ok  upstream remote" || echo "!!  no upstream remote: git remote add upstream git@github.com:usefulappmaker/beeb-android.git"

.PHONY: local-properties
local-properties: ## Write local.properties pointing Gradle at the SDK
	echo "sdk.dir=$(ANDROID_HOME)" > local.properties

.PHONY: keystore
keystore: ## Create the release keystore and .env once (never commit either)
	@test ! -f .env || { echo ".env already exists; delete it first if you really want a new key"; exit 1; }
	@pw=$$(openssl rand -hex 16); ks=$(HOME)/.android/beeb-jon.jks; \
		keytool -genkeypair -keystore $$ks -alias beeb -keyalg RSA -keysize 4096 -validity 10000 \
			-storepass $$pw -keypass $$pw -dname "CN=Beeb (Jon)" && \
		printf 'BEEB_KEYSTORE_PATH=%s\nBEEB_KEYSTORE_PASSWORD=%s\nBEEB_KEY_ALIAS=beeb\n' $$ks $$pw > .env && \
		chmod 600 .env $$ks && echo "Wrote .env and $$ks. Back up both: a new key means uninstalling on the TV."

##@ 2. Build

.PHONY: build
build: ## Build the debug APK ("Beeb (Jon) Preview", $(DEBUG_ID))
	$(GRADLE) assembleDebug

.PHONY: release
release: ## Build the signed release APK ("Beeb (Jon)", $(APP_ID))
	@test -n "$(BEEB_KEYSTORE_PATH)" || { echo "No signing config: make keystore"; exit 1; }
	$(GRADLE) assembleRelease
	@echo "APK: $(RELEASE_APK)"

.PHONY: clean
clean: ## Delete build outputs
	$(GRADLE) clean

##@ 3. Test

.PHONY: test
test: ## Run JVM unit tests (no device needed)
	$(GRADLE) testDebugUnitTest

.PHONY: lint
lint: ## Run Android lint
	$(GRADLE) lintDebug

.PHONY: test-device
test-device: ## Run instrumented tests on the connected device/emulator (DEVICE=)
	$(GRADLE) connectedDebugAndroidTest

##@ 4. Living-room TV

.PHONY: tv-connect
tv-connect: ## Connect to the TV (TV=) and print its serial
	@test -n "$(TV)" || { echo "Set TV=<ip or mDNS name> in local.mk"; exit 1; }
	@$(WIFI_DEVICE) "$(TV)"

.PHONY: install-tv
install-tv: release ## Install the signed release on the TV without launching it
	@serial=$$($(WIFI_DEVICE) "$(TV)") && $(ADB) -s $$serial install -r $(RELEASE_APK)

.PHONY: run-tv
run-tv: install-tv ## Build, install and launch the release on the TV (the smoke-test loop)
	@serial=$$($(WIFI_DEVICE) "$(TV)") && $(ADB) -s $$serial shell am start -n $(call ACTIVITY,$(APP_ID))

.PHONY: run-tv-debug
run-tv-debug: build ## Install and launch the debug build on the TV (needed for make prefs)
	@serial=$$($(WIFI_DEVICE) "$(TV)") && $(ADB) -s $$serial install -r $(DEBUG_APK) && \
		$(ADB) -s $$serial shell am start -n $(call ACTIVITY,$(DEBUG_ID))

.PHONY: logs
logs: ## Follow the release app's logcat on the TV (Ctrl-C to quit)
	@serial=$$($(WIFI_DEVICE) "$(TV)") && $(ADB) -s $$serial logcat --pid=$$($(ADB) -s $$serial shell pidof -s $(APP_ID))

.PHONY: screenshot
screenshot: ## Save a TV screenshot to screenshot.png
	@serial=$$($(WIFI_DEVICE) "$(TV)") && $(ADB) -s $$serial exec-out screencap -p > screenshot.png && echo "Saved screenshot.png"

.PHONY: version-tv
version-tv: ## Show which Beeb builds are installed on the TV, and their versions
	@serial=$$($(WIFI_DEVICE) "$(TV)") && for p in org.beebplayer.android $(APP_ID) $(DEBUG_ID); do \
		v=$$($(ADB) -s $$serial shell dumpsys package $$p | grep -m1 versionName | tr -d ' '); \
		echo "$$p $${v:-(not installed)}"; done

##@ 5. Inspect data

.PHONY: prefs
prefs: ## Print the debug build's stored profiles, saved and history (SharedPreferences XML)
	@serial=$$($(WIFI_DEVICE) "$(TV)") && $(ADB) -s $$serial shell run-as $(DEBUG_ID) cat shared_prefs/BeebLocal.xml

##@ 6. Upstream

.PHONY: upstream
upstream: ## Fetch upstream and show commits not yet in this branch
	git fetch upstream
	@git log --oneline HEAD..upstream/main || true
