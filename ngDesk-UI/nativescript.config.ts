import { NativeScriptConfig } from '@nativescript/core';

export default {
	id: 'com.ngdesk.ngdesk2',
	main: 'app.js',
	appResourcesPath: 'App_Resources',
	webpackConfigPath: 'webpack.config.js',
	ios: {
		discardUncaughtJsExceptions: true,
	},
	android: {
		discardUncaughtJsExceptions: true,
		v8Flags: '--expose_gc',
		markingMode: 'none',
		suppressCallJSMethodExceptions: false,
	},
	appPath: 'src',
} as NativeScriptConfig;
