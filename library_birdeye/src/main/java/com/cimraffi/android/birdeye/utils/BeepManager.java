/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cimraffi.android.birdeye.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import com.cimraffi.android.birdeye.R;
import java.io.Closeable;
import java.io.IOException;

/**
 * Manages beeps and vibrations for {@link //CaptureActivity}. 管理声音和震动
 */
public final class BeepManager implements MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener, Closeable {

	private static final String TAG = BeepManager.class.getSimpleName();

	private static final float BEEP_VOLUME = 0.8f;
	private static final long VIBRATE_DURATION = 200L;

	private final Activity activity;
	private MediaPlayer mediaPlayer;
	private Vibrator vibrator;
	private boolean playBeep;
	private boolean vibrate;

	private boolean hasStop = true;

	public BeepManager(Activity activity) {
		this.activity = activity;
		this.mediaPlayer = null;
		updatePrefs();
	}

	public synchronized void updatePrefs() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);
		playBeep = shouldBeep(prefs, activity);
		vibrate = prefs.getBoolean("preferences_vibrate", true);
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			// 设置activity音量控制键控制的音频流
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = buildMediaPlayer(activity);
		}
	}

	/**
	 * 开启响铃和震动
	 */
	public synchronized void playBeepSound() {
		if (playBeep && mediaPlayer != null) {
			hasStop = true;
			mediaPlayer.start();
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
		}
	}

	/**
	 * 开启响铃和震动
	 */
	public synchronized void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			hasStop = false;
			mediaPlayer.start();
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
		}
		if (vibrate) {
			vibrator = (Vibrator) activity
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(new long[]{VIBRATE_DURATION,VIBRATE_DURATION},0);
		}
	}

	public synchronized void stopBeepSoundAndVibrate(){
		if (playBeep && mediaPlayer != null) {
			hasStop = true;
			mediaPlayer.stop();
		}
		if (vibrator!=null){
			vibrator.cancel();
		}
	}

	/**
	 * 判断是否需要响铃
	 *
	 * @param prefs
	 * @param activity
	 * @return
	 */
	private static boolean shouldBeep(SharedPreferences prefs, Context activity) {
		boolean shouldPlayBeep = prefs.getBoolean(
				"preferences_play_beep", true);
		if (shouldPlayBeep) {
			// See if sound settings overrides this
			AudioManager audioService = (AudioManager) activity
					.getSystemService(Context.AUDIO_SERVICE);
			if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
				shouldPlayBeep = false;
			}
		}
		return shouldPlayBeep;
	}

	/**
	 * 创建MediaPlayer
	 * 
	 * @param activity
	 * @return
	 */
	private MediaPlayer buildMediaPlayer(Context activity) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// 监听是否播放完成
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		// 配置播放资源
		try {
			AssetFileDescriptor file = activity.getResources()
					.openRawResourceFd(R.raw.beep);//
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
			} finally {
				file.close();
			}
			// 设置音量
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare();
			return mediaPlayer;
		} catch (IOException ioe) {
			Log.w(TAG,"IOException", ioe);
			mediaPlayer.release();
			return null;
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// When the beep has finished playing, rewind to queue up another one.
		mp.seekTo(0);
		if (mp!=null&&!hasStop){
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						mp.start();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			},200);
		}


	}

	@Override
	public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
		if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			// we are finished, so put up an appropriate error toast if required
			// and finish
			activity.finish();
		} else {
			// possibly media player error, so release and recreate
			mp.release();
			mediaPlayer = null;
			updatePrefs();
		}
		return true;
	}

	@Override
	public synchronized void close() {
		if (mediaPlayer != null) {
			hasStop = true;
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (vibrator!=null){
			vibrator.cancel();
			vibrator = null;
		}
	}

}
