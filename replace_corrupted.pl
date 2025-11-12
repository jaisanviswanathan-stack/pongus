#!/usr/bin/perl
use strict;
use warnings;

my $file = 'PingPongGame.java';

# Read as binary
open(my $fh, '<', $file) or die "Cannot open: $!";
binmode($fh);
my $content = do { local $/; <$fh> };
close($fh);

my $original_len = length($content);
print "Original file size: $original_len bytes\n\n";

my $count = 0;

# The corrupted sequences in hex form extracted from the file
# UP arrow: c383c692c386e28099c383e280a0c3a2e282ace284a2c383c692c3a2e282acc5a1c383e2809ac382c2a2c383c692c386e28099c383e2809ac382c2a2c383c692c382c2a2c383c2a2c3a2e2809ac2acc385c2a1c383e2809ac382c2acc383c692c3a2e282acc5a1c383e2809ac382c2a0c383c692c386e28099c383e2809ac382c2a2c383c692c382c2a2c383c2a2c3a2e2809ac2acc385c2a1c383e2809ac382c2acc383c692c3a2e282acc2b9c383e280a6c3a2e282acc593

# Let me build the byte string from hex
my $corrupted_up = pack("H*", "c383c692c386e28099c383e280a0c3a2e282ace284a2c383c692c3a2e282acc5a1c383e2809ac382c2a2c383c692c386e28099c383e2809ac382c2a2c383c692c382c2a2c383c2a2c3a2e2809ac2acc385c2a1c383e2809ac382c2acc383c692c3a2e282acc5a1c383e2809ac382c2a0c383c692c386e28099c383e2809ac382c2a2c383c692c382c2a2c383c2a2c3a2e2809ac2acc385c2a1c383e2809ac382c2acc383c692c3a2e282acc2b9c383e280a6c3a2e282acc593");

print "Looking for UP arrow pattern...\n";
my $idx = index($content, $corrupted_up);
if ($idx >= 0) {
    print "Found UP arrow at position $idx\n";
    $count += ($content =~ s/\Q$corrupted_up\E/^/g);
} else {
    print "UP arrow pattern not found\n";
}

# Write back
open($fh, '>', $file) or die "Cannot write: $!";
binmode($fh);
print $fh $content;
close($fh);

print "\nReplacements made: $count\n";
print "New file size: " . length($content) . " bytes\n";
