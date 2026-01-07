<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use App\Models\Call;
use App\Models\Transaction;
use Illuminate\Support\Facades\DB;

class ClearCallsData extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'calls:clear {--force : Skip confirmation}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Clear all calls data from the database';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        if (!$this->option('force')) {
            if (!$this->confirm('⚠️  This will delete ALL calls data. Are you sure?')) {
                $this->info('Operation cancelled.');
                return 0;
            }
        }

        $this->info('Starting to clear calls data...');

        DB::beginTransaction();

        try {
            // Get counts before deletion
            $callsCount = Call::count();
            $transactionsCount = Transaction::where('reference_type', 'CALL')->count();

            $this->info("Found {$callsCount} calls and {$transactionsCount} call-related transactions");

            // Delete call-related transactions first
            $this->info('Deleting call-related transactions...');
            Transaction::where('reference_type', 'CALL')->delete();
            $this->info('✅ Call-related transactions deleted');

            // Delete all calls
            $this->info('Deleting all calls...');
            Call::query()->delete();
            $this->info('✅ All calls deleted');

            // Reset user busy status
            $this->info('Resetting user busy status...');
            DB::table('users')->update(['is_busy' => false]);
            $this->info('✅ User busy status reset');

            DB::commit();

            $this->newLine();
            $this->info('🎉 Successfully cleared all calls data!');
            $this->info("   - Deleted {$callsCount} calls");
            $this->info("   - Deleted {$transactionsCount} call-related transactions");
            $this->info("   - Reset all user busy statuses");

            return 0;

        } catch (\Exception $e) {
            DB::rollBack();
            $this->error('❌ Error clearing calls data: ' . $e->getMessage());
            return 1;
        }
    }
}




