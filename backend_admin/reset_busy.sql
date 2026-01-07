UPDATE users SET is_busy = 0 WHERE user_type = 'FEMALE';
SELECT id, name, is_busy, audio_call_enabled, video_call_enabled FROM users WHERE user_type = 'FEMALE' LIMIT 5;





