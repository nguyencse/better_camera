// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugins.camera.CameraPermissions.PermissionsRegistry;
import io.flutter.view.TextureRegistry;

/**
 * Platform implementation of the camera_plugin.
 *
 * <p>Instantiate this in an add to app scenario to gracefully handle activity and context changes.
 * See {@code io.flutter.plugins.camera.MainActivity} for an example.
 *
 * <p>Call {@link #registerWith(Registrar)} to register an implementation of this that uses the
 * stable {@code io.flutter.plugin.common} package.
 */
public final class CameraPlugin implements FlutterPlugin, ActivityAware {

    private static final String TAG = "CameraPlugin";
    private @Nullable FlutterPluginBinding flutterPluginBinding;
    private @Nullable MethodCallHandlerImpl methodCallHandler;

    /**
     * Initialize this within the {@code #configureFlutterEngine} of a Flutter activity or fragment.
     *
     * <p>See {@code io.flutter.plugins.camera.MainActivity} for an example.
     */
    public CameraPlugin() {
    }

    /**
     * Registers a plugin implementation that uses the stable {@code io.flutter.plugin.common}
     * package.
     *
     * <p>Calling this automatically initializes the plugin. However plugins initialized this way
     * won't react to changes in activity or context, unlike {@link CameraPlugin}.
     */
    public static void registerWith(Registrar registrar) {
        CameraPlugin plugin = new CameraPlugin();
        plugin.maybeStartListening(
                registrar.activity(),
                registrar.messenger(),
                registrar::addRequestPermissionsResultListener,
                registrar.view());
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        this.flutterPluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        this.flutterPluginBinding = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        if (flutterPluginBinding != null) {
            maybeStartListening(
                    binding.getActivity(),
                    flutterPluginBinding.getFlutterEngine().getDartExecutor(),
                    binding::addRequestPermissionsResultListener,
                    flutterPluginBinding.getFlutterEngine().getRenderer());
        }
    }

    @Override
    public void onDetachedFromActivity() {
        if (methodCallHandler == null) {
            // Could be on too low of an SDK to have started listening originally.
            return;
        }

        methodCallHandler.stopListening();
        methodCallHandler = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    private void maybeStartListening(
            Activity activity,
            BinaryMessenger messenger,
            PermissionsRegistry permissionsRegistry,
            TextureRegistry textureRegistry) {

        methodCallHandler =
                new MethodCallHandlerImpl(
                        activity, messenger, new CameraPermissions(), permissionsRegistry, textureRegistry);
    }
}
