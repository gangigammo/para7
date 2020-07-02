#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : enable

int addr(const int width, const int height, int x, int y){
  if(y<0){y=0;}
  if(height-1<y){y=height-1;}
  if(x<0){x=0;}
  if(width-1<x){x=width-1;}
  return (y*width*3+x*3);
}

float bound(const float in){
  if(in<0) return 0;
  if(in>255) return 255.0f;
  return in;
}


float filter10(__global const uchar* in, const int width, const int height,
           const int lx,const int ly, const int shift,const float s){
           float a;
           float b;
           a = (in[addr(width, height, lx-1, ly-1)+shift] * (-1))  +
                         in[addr(width, height, lx  , ly-1)+shift] * 0 +
                         (in[addr(width, height, lx+1, ly-1)+shift] * 1) +

                         (in[addr(width, height, lx-1, ly)+shift] * (-2)) +
                         in[addr(width, height, lx  , ly)+shift] * 0+
                         (in[addr(width, height, lx+1, ly)+shift] * 2) +

                         (in[addr(width, height, lx-1, ly+1)+shift] * (-1)) +
                         in[addr(width, height, lx  , ly+1)+shift] * 0+
                         (in[addr(width, height, lx+1, ly+1)+shift] * 1);

           b = (in[addr(width, height, lx-1, ly-1)+shift] * (-1))  +
                                   in[addr(width, height, lx  , ly-1)+shift] * (-2) +
                                   (in[addr(width, height, lx+1, ly-1)+shift] * (-1)) +

                                   (in[addr(width, height, lx-1, ly)+shift] * 0) +
                                   in[addr(width, height, lx  , ly)+shift] * 0 +
                                   (in[addr(width, height, lx+1, ly)+shift] * 0) +

                                   (in[addr(width, height, lx-1, ly+1)+shift] * 1) +
                                   in[addr(width, height, lx  , ly+1)+shift] * 2+
                                   (in[addr(width, height, lx+1, ly+1)+shift] * 1);
  return ((a * a + b * b) *s/1600);

}


// OpenCL Kernel Function
__kernel void Filter10(const int width, const int height,
                     __global const uchar* in,
                     __global uchar *outb,
		     const float scale) {
  // get index of global data array
  int lx = get_global_id(0);
  int ly = get_global_id(1);
/*
  // bound check (equivalent to the limit on a 'for' loop for standard/serial C code
  if (lx > width || ly >height)  {
    return;
  }
*/
  float samp = scale/50;
  int add = addr(width,height,lx,ly);
  int oadd = (ly*width+lx)*4;
  outb[oadd  ]= bound(filter10(in,width,height,lx,ly,0,scale)*samp+128);
  outb[oadd+1]= bound(filter10(in,width,height,lx,ly,1,scale)*samp+128);
  outb[oadd+2]= bound(filter10(in,width,height,lx,ly,2,scale)*samp+128);

  outb[oadd  ]= bound(in[add]+filter10(in,width,height,lx,ly,0,scale)*samp);
  outb[oadd+1]= bound(in[add+1]+filter10(in,width,height,lx,ly,1,scale)*samp);
  outb[oadd+2]= bound(in[add+2]+filter10(in,width,height,lx,ly,2,scale)*samp);

  outb[oadd  ]= bound(filter10(in,width,height,lx,ly,0,scale)*samp);
    outb[oadd+1]= bound(filter10(in,width,height,lx,ly,1,scale)*samp);
    outb[oadd+2]= bound(filter10(in,width,height,lx,ly,2,scale)*samp);

  outb[oadd+3]= 255;
}

__kernel void Filter9(const int width, const int height,
                     __global const uchar* in,
                     __global uchar *outb,
		     const float cx) {
  // get index of global data array
  int lx = get_global_id(0);
  int ly = get_global_id(1);

/*
  // bound check (equivalent to the limit on a 'for' loop for standard/serial C code
  if (lx > width || ly >height)  {
    return;
  }
*/
  int cxi = (int)cx/16;
  int oadd = (ly*width+lx)*4;
  int iadd = addr(width, height, lx+1, ly+1);
  int g = (in[iadd+0]*0.072169f+in[iadd+1]*0.715160f+in[iadd+2]*0.212671f)/cx;
  g = g*cx;
  outb[oadd  ]= g;
  outb[oadd+1]= g;
  outb[oadd+2]= g;
  outb[oadd+3]= 255-g;
}


