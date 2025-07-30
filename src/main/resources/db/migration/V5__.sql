INSERT INTO server_setting_items (setting_key, clazz, setting_value)
    SELECT 'grading.multipleChoicesQuestionsCheckingStrategy', 'java.lang.String', '"all_correct"'
    FROM dual
    WHERE NOT EXISTS (SELECT * FROM server_setting_items WHERE setting_key = 'grading.multipleChoicesQuestionsCheckingStrategy');

INSERT INTO server_setting_items (setting_key, clazz, setting_value)
    SELECT 'grading.enableLosePoints', 'java.lang.Boolean', 'false'
    FROM dual
    WHERE NOT EXISTS (SELECT * FROM server_setting_items WHERE setting_key = 'grading.enableLosePoints');

UPDATE server_setting_items
    SET setting_key = 'generating.samplingStrategy'
    WHERE setting_key = 'generating.drawingStrategy';