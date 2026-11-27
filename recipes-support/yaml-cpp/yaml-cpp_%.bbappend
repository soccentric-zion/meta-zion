# meta-oe's yaml-cpp doesn't ship a native variant. instrumentation-native
# (recipes-instrumentation/instrumentation/instrumentation.bb) needs one to
# build alarmgen, the code-gen tool that recipe requires at cross-build
# time -- confirmed by an actual build ("Nothing PROVIDES 'yaml-cpp-native'").
# Plain cmake recipe, nothing native-unfriendly in it; safe to extend.
BBCLASSEXTEND += "native"
