<?php

return [

    'paths' => [
        realpath(base_path('resources/views'))
    ],

    'compiled' => realpath(storage_path('framework/views'))
        ?: storage_path('framework/views'),

];

