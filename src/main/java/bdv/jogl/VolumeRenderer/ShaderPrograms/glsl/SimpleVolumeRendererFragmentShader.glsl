#version 130
//http://www.visualizationlibrary.org/documentation/pag_guide_raycast_volume.html
uniform sampler3D inVolumeTexture;
uniform sampler1D inColorTexture;
out vec4 fragmentColor;
smooth in vec3 textureCoord;
uniform float inMaxVolumeValue;
uniform float inMinVolumeValue;
uniform vec3 inEyePosition;
float val_threshold =1;


void main(void)
{	
	

	//float volume = textureCoord.x; //texture(inVolumeTexture,textureCoord ).x ;
	//fragmentColor  = vec4(textureCoord,1);
	//fragmentColor = vec4(texture(inVolumeTexture,textureCoord ).r/ inMaxVolumeValue,0.1f,0.1f ,1.f);//vec4(textureCoord,1.f);
	//fragmentColor = vec4(1.f,1.f,1.f,1.f);

	
	const int samples = 128;
	float sample_step =1f/float(samples);
	 const float brightness = 150.0f;
	 
    vec3 ray_dir = normalize(textureCoord - inEyePosition );
    vec3 ray_pos = textureCoord; // the current ray position
    vec3 pos111 = vec3(1.0, 1.0, 1.0);
    vec3 pos000 = vec3(0.0, 0.0, 0.0);

    fragmentColor = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 color;
    float volumeNormalizeFactor = 1.f/ (inMaxVolumeValue-inMinVolumeValue+0.01);
   	for(int i = 0; i< samples;i++){

        // note: 
        // - ray_dir * sample_step can be precomputed
        // - we assume the volume has a cube-like shape
    
  		ray_pos += ray_dir * sample_step;
  		
        // break out if ray reached the end of the cube.
        if (any(greaterThan(ray_pos,pos111)))
            continue;

        if (any(lessThan(ray_pos,pos000)))
            continue;

        float density = (texture(inVolumeTexture, ray_pos).r-inMinVolumeValue) *volumeNormalizeFactor;


        color.rgb = /*vec3(1f,0f,0f);*/ texture1D(inColorTexture, density).rgb;
        color.a   = density * sample_step * val_threshold * brightness;
        fragmentColor.rgb = fragmentColor.rgb * (1.0 - color.a) + color.rgb * color.a;
        
        }
      

    //fragmentColor = vec4(ray_dir,1);
    //fragmentColor = vec4(textureCoord,1);
/*	if(fragmentColor ==vec4(0.0, 0.0, 0.0, 0.0)){
	   fragmentColor = vec4(1.0, 0.0, 0.0, 0.0);
	   //discard;
	}else{*/
	 fragmentColor = vec4 (fragmentColor.rgb,0.1);
	//}
	
	/*fragmentColor=vec4(1-(texture(inVolumeTexture, textureCoord).r-inMinVolumeValue) *volumeNormalizeFactor*7,0,0,1);
	if(fragmentColor == vec4(0,0,0,1)){
		fragmentColor == vec4(0,0,1,1);
	}*/
}
