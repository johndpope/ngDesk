import tinymce from 'tinymce/tinymce';


export const config: any = {
  theme: 'silver',
  resize: false,
  menubar: false,
  // powerpaste advcode toc tinymcespellchecker a11ychecker mediaembed linkchecker help
  plugins: 'print preview searchreplace autolink directionality visualblocks visualchars image imagetools link codesample table charmap hr pagebreak nonbreaking anchor insertdatetime advlist lists wordcount textpattern paste',
  toolbar: 'formatselect | fontselect fontsizeselect | bold italic underline forecolor backcolor | link image | alignleft aligncenter alignright alignjustify numlist bullist outdent indent lineheight | cut copy paste | removeformat table',
  browser_spellcheck: true,
  contextmenu: false,
  image_advtab: true,
  image_title: true,
  imagetools_toolbar: 'rotateleft rotateright | flipv fliph | editimage imageoptions',
  automatic_uploads: true,
  paste_data_images: true,
  /*
  URL of our upload handler (for more details check: https://www.tiny.cloud/docs/configure/file-image-upload/#images_upload_url)
  images_upload_url: 'postAcceptor.php',
  here we add custom filepicker only to Image dialog
  */
 file_picker_types: 'image',
 /* and here's our custom image picker*/
 file_picker_callback: function(cb, value, meta) {
   const input = document.createElement('input');
   input.setAttribute('type', 'file');
   input.setAttribute('accept', 'image/*');

   /*
     Note: In modern browsers input[type="file"] is functional without
     even adding it to the DOM, but that might not be the case in some older
     or quirky browsers like IE, so you might want to add it to the DOM
     just in case, and visually hide it. And do not forget do remove it
     once you do not need it anymore.
   */

   input.onchange = function(this: HTMLInputElement) {
     const file = this.files[0];

     const reader = new FileReader();
     reader.onload = function() {
       /*
         Note: Now we need to register the blob in TinyMCEs image blob
         registry. In the next release this part hopefully won't be
         necessary, as we are looking to handle it internally.
       */
       const id = 'blobid' + new Date().getTime();
       const blobCache = tinymce.activeEditor.editorUpload.blobCache;
       const base64 = (reader.result as string).split(',')[1];
       const blobInfo = blobCache.create(id, file, base64);
       blobCache.add(blobInfo);

       /* call the callback and populate the Title field with the file name */
       cb(blobInfo.blobUri(), { title: file.name });
     };
     reader.readAsDataURL(file);
   };

   input.click();
 },
 convert_urls: false
};

