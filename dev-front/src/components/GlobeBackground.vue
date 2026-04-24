<template>
  <div ref="el" class="pointer-events-none fixed inset-0 z-0" />
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as THREE from 'three'

const el = ref()

let renderer
let animationId
let handleResize
const globeOffsetX = -0.72

const createGlowTexture = () => {
  const canvas = document.createElement('canvas')
  canvas.width = 128
  canvas.height = 128
  const ctx = canvas.getContext('2d')
  const gradient = ctx.createRadialGradient(64, 64, 0, 64, 64, 64)
  gradient.addColorStop(0, 'rgba(186, 230, 253, 1)')
  gradient.addColorStop(0.25, 'rgba(56, 189, 248, 0.95)')
  gradient.addColorStop(0.6, 'rgba(14, 165, 233, 0.35)')
  gradient.addColorStop(1, 'rgba(14, 165, 233, 0)')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, 128, 128)
  return new THREE.CanvasTexture(canvas)
}

const createLabelSprite = (text) => {
  const canvas = document.createElement('canvas')
  canvas.width = 384
  canvas.height = 112
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.font = '700 34px "Microsoft YaHei", "PingFang SC", "Segoe UI", sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.strokeStyle = 'rgba(2, 6, 23, 0.95)'
  ctx.lineWidth = 6
  ctx.strokeText(text, canvas.width / 2, canvas.height / 2)
  const palette = [
    '#67e8f9',
    '#93c5fd',
    '#c4b5fd',
    '#f9a8d4',
    '#fcd34d',
    '#6ee7b7',
    '#fdba74',
    '#7dd3fc',
  ]
  const color = palette[text.charCodeAt(0) % palette.length]
  ctx.fillStyle = color
  ctx.shadowColor = color
  ctx.shadowBlur = 30
  ctx.fillText(text, canvas.width / 2, canvas.height / 2)

  const texture = new THREE.CanvasTexture(canvas)
  texture.needsUpdate = true
  texture.generateMipmaps = false
  texture.minFilter = THREE.LinearFilter
  texture.magFilter = THREE.LinearFilter
  const material = new THREE.SpriteMaterial({
    map: texture,
    transparent: true,
    depthWrite: false,
    depthTest: false,
    opacity: 1,
  })
  const sprite = new THREE.Sprite(material)
  sprite.scale.set(0.62, 0.2, 1)
  return sprite
}

onMounted(() => {
  const scene = new THREE.Scene()

  const camera = new THREE.PerspectiveCamera(
    60,
    window.innerWidth / window.innerHeight,
    0.1,
    1000,
  )
  camera.position.z = 3

  renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(window.innerWidth, window.innerHeight)
  el.value.appendChild(renderer.domElement)

  const globe = new THREE.Mesh(
    new THREE.SphereGeometry(1, 64, 64),
    new THREE.MeshBasicMaterial({
      color: 0x0b1120,
      wireframe: true,
      transparent: true,
      opacity: 0.2,
    }),
  )
  globe.position.x = globeOffsetX
  scene.add(globe)

  // Major global financial centers by region
  const cities = [
    // Asia
    { cn: '香港', lat: 22.3193, lng: 114.1694 },
    { cn: '新加坡', lat: 1.3521, lng: 103.8198 },
    { cn: '东京', lat: 35.6762, lng: 139.6503 },
    { cn: '上海', lat: 31.2304, lng: 121.4737 },
    { cn: '深圳', lat: 22.5431, lng: 114.0579 },
    { cn: '首尔', lat: 37.5665, lng: 126.978 },

    // United States
    { cn: '纽约', lat: 40.7128, lng: -74.006 },
    { cn: '芝加哥', lat: 41.8781, lng: -87.6298 },
    { cn: '旧金山', lat: 37.7749, lng: -122.4194 },
    { cn: '波士顿', lat: 42.3601, lng: -71.0589 },

    // Europe
    { cn: '伦敦', lat: 51.5074, lng: -0.1278 },
    { cn: '法兰克福', lat: 50.1109, lng: 8.6821 },
    { cn: '苏黎世', lat: 47.3769, lng: 8.5417 },
    { cn: '巴黎', lat: 48.8566, lng: 2.3522 },
    { cn: '阿姆斯特丹', lat: 52.3676, lng: 4.9041 },

    // Middle East
    { cn: '迪拜', lat: 25.2048, lng: 55.2708 },
    { cn: '阿布扎比', lat: 24.4539, lng: 54.3773 },
    { cn: '多哈', lat: 25.2854, lng: 51.531 },
    { cn: '利雅得', lat: 24.7136, lng: 46.6753 },
  ]

  const latLngToVec = (lat, lng) => {
    const phi = ((90 - lat) * Math.PI) / 180
    const theta = ((lng + 180) * Math.PI) / 180

    return new THREE.Vector3(
      -Math.sin(phi) * Math.cos(theta),
      Math.cos(phi),
      Math.sin(phi) * Math.sin(theta),
    )
  }

  const group = new THREE.Group()
  const glowTexture = createGlowTexture()
  const markers = []
  const globeCenter = new THREE.Vector3(globeOffsetX, 0, 0)
  const viewDirection = new THREE.Vector3()
  const worldPos = new THREE.Vector3()

  cities.forEach((city) => {
    const direction = latLngToVec(city.lat, city.lng).normalize()
    const anchor = new THREE.Object3D()
    anchor.position.copy(direction.clone().multiplyScalar(1.01))

    const coreDot = new THREE.Mesh(
      new THREE.SphereGeometry(0.011, 10, 10),
      new THREE.MeshBasicMaterial({ color: 0xe0fbff }),
    )
    anchor.add(coreDot)

    const glow = new THREE.Sprite(
      new THREE.SpriteMaterial({
        map: glowTexture,
        color: 0x22d3ee,
        transparent: true,
        blending: THREE.AdditiveBlending,
        depthWrite: false,
        opacity: 1,
      }),
    )
    glow.scale.set(0.12, 0.12, 1)
    anchor.add(glow)

    const label = createLabelSprite(city.cn)
    label.position.copy(direction.clone().multiplyScalar(0.15))
    anchor.add(label)

    group.add(anchor)
    markers.push({ anchor, glow, label })
  })

  scene.add(group)
  group.position.x = globeOffsetX

  handleResize = () => {
    const width = window.innerWidth
    const height = window.innerHeight
    camera.aspect = width / height
    camera.updateProjectionMatrix()
    renderer.setSize(width, height)
  }

  window.addEventListener('resize', handleResize)

  const animate = () => {
    animationId = requestAnimationFrame(animate)
    globe.rotation.y += 0.002
    group.rotation.y += 0.002

    viewDirection.copy(camera.position).sub(globeCenter).normalize()
    markers.forEach((marker) => {
      marker.anchor.getWorldPosition(worldPos)
      const normal = worldPos.sub(globeCenter).normalize()
      const isFrontSide = normal.dot(viewDirection) > -0.1
      marker.label.visible = isFrontSide
      marker.glow.material.opacity = isFrontSide ? 1 : 0.35
    })

    renderer.render(scene, camera)
  }

  animate()
})

onBeforeUnmount(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (handleResize) window.removeEventListener('resize', handleResize)
  if (renderer) renderer.dispose()
})
</script>
