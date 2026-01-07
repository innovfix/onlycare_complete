<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use App\Models\User;

class DisableCreatorCalls extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'creators:disable-calls';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Disable audio and video calls for all creators (FEMALE users)';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Disabling audio and video calls for all creators...');

        // Find all creators (FEMALE users)
        $query = User::where('user_type', 'FEMALE')
                     ->orWhere('gender', 'FEMALE'); // Cover both cases just to be safe
        
        $count = $query->count();
        
        if ($count === 0) {
            $this->info('No creators found.');
            return;
        }

        if ($this->confirm("Found {$count} creators. Do you want to disable audio and video calls for all of them?", true)) {
            $updated = $query->update([
                'audio_call_enabled' => false,
                'video_call_enabled' => false
            ]);

            $this->info("Successfully updated {$updated} creators.");
        } else {
            $this->info('Operation cancelled.');
        }
    }
}




