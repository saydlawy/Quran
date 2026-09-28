# Ultimate Mushaf — release-gated audio layer

The audio subsystem is service-owned and Media3-based. UI lifecycle is not the source of truth.

Required release gates:
- process/activity recreation does not create a second player
- MediaSession exposes transport controls
- playback state is persisted
- downloaded assets are preferred over remote URLs
- timing metadata maps audio to canonical verse/page/word identities
- no audio asset is bundled or redistributed without provenance/license verification
