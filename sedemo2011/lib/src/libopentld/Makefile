MCC=$(MATLABROOT)/bin/mcc
MBUILD=$(MATLABROOT)/bin/mbuild
CPP_TARGETS=libopentld
ARCH=`uname -m`
STAND_ALONE_TARGETS=$(CPP_TARGETS)

build_all: $(STAND_ALONE_TARGETS)

TLD_BBOX=bbox/bb_burn.m  bbox/bb_center.m  bbox/bb_click.m  bbox/bb_click_move.m  bbox/bb_cluster_confidence.m  bbox/bb_cluster.m  bbox/bb_correct.m  bbox/bb_distance.m  bbox/bb_draw.m  bbox/bb_get_similar.m  bbox/bb_height.m  bbox/bb_hull.m  bbox/bb_isdef.m  bbox/bb_isin.m  bbox/bb_isout.m  bbox/bb_normalize.m  bbox/bb_points.m  bbox/bb_predict.m  bbox/bb_rescale_absolute.m  bbox/bb_rescale_relative.m  bbox/bb_scale.m  bbox/bb_scan.m  bbox/bb_shift_absolute.m  bbox/bb_shift_relative.m  bbox/bb_size.m  bbox/bb_square.m  bbox/bb_width.m

TLD_IMG=img/cam_record.m  img/cam_wait.m img/img_alloc.m  img/img_any2any.m  img/img_blur.m  img/img_dir.m  img/img_get.m  img/img_merge_frames.m  img/img_patch.m img/img_get_from_file.m

TLD_OTHER=other/init_workspace.m other/stabilize.m  other/usbclear.m  other/usbinit.m

TLD_TLD=tld/tldDetection.m  tld/tldDisplay.m  tld/tldExample.m  tld/tldGenerateAprioriData.m  tld/tldGenerateFeatures.m  tld/tldGenerateNegativeData.m  tld/tldGeneratePositiveData.m  tld/tldGeneratePositiveData_single_bbox.m  tld/tldGetPattern_fast.m  tld/tldGetPattern.m  tld/tldInitDetector.m  tld/tldInitFirstFrame.m  tld/tldInit.m  tld/tldInitSource.m  tld/tldLearning.m  tld/tldLoadAprioriImages.m  tld/tldMinMaxVar.m  tld/tldNN.m  tld/tldPatch2Pattern.m  tld/tldProcessFrame.m  tld/tldSaveImages.m  tld/tldSplitNegativeData.m  tld/tldTracking.m  tld/tldTrack.m  tld/tldTrack_occlusion_2.m  tld/tldTrack_occlusion.m  tld/tldTrainNN.m  tld/tldUpdateDetectorConservative.m  tld/tldUpdateDetector.m tld/tldExampleInit.m tld/tldKeyPressHandler.m tld/tldProcessSingleFrame.m tld/tldExampleInitDefault.m

TLD_UTILS=utils/idx2id.m  utils/mat2img.m  utils/maximize.m  utils/median2.m  utils/n2s.m  utils/ntuples.m  utils/pseudorandom_indexes.m  utils/randvalues.m  utils/repcel.m  utils/uniquecount.m  utils/vnormp.m

TLD_MAIN=initcamera.m

ifeq ($(ARCH),'x86_64')
	TLD_MEX=mex/bb_overlap.mexa64 mex/distance.mexa64 mex/fern.mexa64 mex/linkagemex.mexa64 mex/lk.mexa64 mex/warp.mexa64
else
	TLD_MEX=mex/bb_overlap.mexglx mex/distance.mexglx mex/fern.mexglx mex/linkagemex.mexglx mex/lk.mexglx mex/warp.mexglx
endif

# The following are the definitions for each target individually.
opentld: $(TLD_MAIN) $(TLD_BBOX) $(TLD_IMG) $(TLD_OTHER) $(TLD_TLD) $(TLD_UTILS)
	$(MCC) -m $^ -o $@ $(TLD_MEX)
	@rm -f *.o*

# The following are the definitions for each target individually.
libopentld: $(TLD_MAIN) $(TLD_BBOX) $(TLD_IMG) $(TLD_OTHER) $(TLD_TLD) $(TLD_UTILS)
	$(MCC) -B csharedlib:$@ $^ $(TLD_MEX)
	@rm -f *.o*

clean:
	rm -f $(STAND_ALONE_TARGETS)

