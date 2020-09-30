for arch in arm64-v8a armeabi-v7a
#armeabi-v7a 
#arm64-v8a
do
    bash build_ffmpeg_merge.sh $arch
done
